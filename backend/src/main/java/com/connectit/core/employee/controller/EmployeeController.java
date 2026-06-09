package com.connectit.core.employee.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.employee.dto.EmployeeRequest;
import com.connectit.core.employee.dto.EmployeeResponse;
import com.connectit.core.employee.dto.UserProvisionRequest;
import com.connectit.core.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponse> responses = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", responses));
    }

    @GetMapping("/unprovisioned")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getUnprovisionedEmployees() {
        List<EmployeeResponse> responses = employeeService.getUnprovisionedEmployees();
        return ResponseEntity.ok(ApiResponse.success("Unprovisioned employees retrieved successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.success("Employee created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @PostMapping("/provision")
    public ResponseEntity<ApiResponse<Void>> provisionUser(@RequestBody UserProvisionRequest request) {
        employeeService.provisionUser(request);
        return ResponseEntity.ok(ApiResponse.success("User login provisioned successfully", null));
    }
}
