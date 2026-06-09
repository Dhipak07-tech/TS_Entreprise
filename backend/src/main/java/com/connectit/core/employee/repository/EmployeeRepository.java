package com.connectit.core.employee.repository;

import com.connectit.core.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByCompanyId(Long companyId);

    Page<Employee> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Employee> findByEmployeeCodeAndCompanyId(String employeeCode, Long companyId);

    Optional<Employee> findByEmailAndCompanyId(String email, Long companyId);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND NOT EXISTS (SELECT u FROM User u WHERE u.employeeId = e.id)")
    List<Employee> findUnprovisionedEmployees(@Param("companyId") Long companyId);
}
