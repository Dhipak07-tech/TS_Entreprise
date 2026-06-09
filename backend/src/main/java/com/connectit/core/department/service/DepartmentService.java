package com.connectit.core.department.service;

import com.connectit.core.department.dto.DepartmentRequest;
import com.connectit.core.department.entity.Department;
import com.connectit.core.department.repository.DepartmentRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Department not found."));
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Error: Department code already exists.");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Error: Manager user not found."));
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .manager(manager)
                .build();

        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department department = getDepartmentById(id);

        if (!department.getCode().equals(request.getCode()) && departmentRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Error: Department code already exists.");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Error: Manager user not found."));
        }

        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setManager(manager);

        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Error: Department not found.");
        }
        departmentRepository.deleteById(id);
    }
}
