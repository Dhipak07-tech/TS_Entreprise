package com.connectit.core.employee.service;

import com.connectit.config.tenant.TenantContext;
import com.connectit.core.department.entity.Department;
import com.connectit.core.department.repository.DepartmentRepository;
import com.connectit.core.employee.dto.EmployeeRequest;
import com.connectit.core.employee.dto.EmployeeResponse;
import com.connectit.core.employee.dto.UserProvisionRequest;
import com.connectit.core.employee.entity.Employee;
import com.connectit.core.employee.repository.EmployeeRepository;
import com.connectit.core.rbac.entity.Role;
import com.connectit.core.rbac.repository.RoleRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.entity.UserProfile;
import com.connectit.core.user.repository.UserProfileRepository;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) companyId = 1L;

        Page<Employee> employees = employeeRepository.findByCompanyId(companyId, pageable);
        return employees.map(this::mapToResponse);
    }

    public List<EmployeeResponse> getUnprovisionedEmployees() {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) companyId = 1L;

        List<Employee> employees = employeeRepository.findUnprovisionedEmployees(companyId);
        return employees.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) companyId = 1L;

        Employee employee = employeeRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found or unauthorized"));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) companyId = 1L;

        // Check for duplicates
        if (employeeRepository.findByEmployeeCodeAndCompanyId(request.getEmployeeCode(), companyId).isPresent()) {
            throw new RuntimeException("Employee with code " + request.getEmployeeCode() + " already exists");
        }
        if (employeeRepository.findByEmailAndCompanyId(request.getEmail(), companyId).isPresent()) {
            throw new RuntimeException("Employee with email " + request.getEmail() + " already exists");
        }

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findByIdAndCompanyId(request.getManagerId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
        }

        Employee employee = Employee.builder()
                .companyId(companyId)
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(dept)
                .jobTitle(request.getJobTitle())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .manager(manager)
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) companyId = 1L;

        Employee employee = employeeRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found or unauthorized"));

        // Validate code unique
        if (!employee.getEmployeeCode().equalsIgnoreCase(request.getEmployeeCode())) {
            if (employeeRepository.findByEmployeeCodeAndCompanyId(request.getEmployeeCode(), companyId).isPresent()) {
                throw new RuntimeException("Employee with code " + request.getEmployeeCode() + " already exists");
            }
        }
        // Validate email unique
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (employeeRepository.findByEmailAndCompanyId(request.getEmail(), companyId).isPresent()) {
                throw new RuntimeException("Employee with email " + request.getEmail() + " already exists");
            }
        }

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findByIdAndCompanyId(request.getManagerId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
        }

        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(dept);
        employee.setJobTitle(request.getJobTitle());
        employee.setStatus(request.getStatus());
        employee.setManager(manager);

        Employee saved = employeeRepository.save(employee);

        // State Synchronization: If employee is deactivated, automatically disable system user account
        Optional<User> userOpt = userRepository.findByEmployeeId(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean newActive = "ACTIVE".equalsIgnoreCase(saved.getStatus());
            if (user.getIsActive() != newActive) {
                user.setIsActive(newActive);
                userRepository.save(user);
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void provisionUser(UserProvisionRequest request) {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) companyId = 1L;

        Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found or unauthorized"));

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new RuntimeException("Cannot provision system access for inactive or terminated employee");
        }

        // Check if employee already has a user account
        if (userRepository.findByEmployeeId(employee.getId()).isPresent()) {
            throw new RuntimeException("Employee already has a provisioned user account");
        }

        // Check if username/email already taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new RuntimeException("Email already linked to another user account");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null) {
            for (String rName : request.getRoles()) {
                Role role = roleRepository.findByName(rName)
                        .orElseThrow(() -> new RuntimeException("Role " + rName + " not found"));
                roles.add(role);
            }
        }
        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default USER role not found"));
            roles.add(defaultRole);
        }

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        } else {
            dept = employee.getDepartment();
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(employee.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .mfaEnabled(false)
                .companyId(companyId)
                .employeeId(employee.getId())
                .passwordResetRequired(true)
                .department(dept)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .phone(employee.getPhone())
                .preferredLanguage("en")
                .build();
        userProfileRepository.save(profile);
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        boolean isProvisioned = userRepository.findByEmployeeId(employee.getId()).isPresent();
        return EmployeeResponse.builder()
                .id(employee.getId())
                .companyId(employee.getCompanyId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .jobTitle(employee.getJobTitle())
                .status(employee.getStatus())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getFirstName() + " " + employee.getManager().getLastName() : null)
                .isProvisioned(isProvisioned)
                .createdAt(employee.getCreatedAt())
                .build();
    }
}
