package com.connectit.core.employee.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private Long companyId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String jobTitle;
    private String status;
    private Long managerId;
    private String managerName;
    private boolean isProvisioned;
    private LocalDateTime createdAt;
}
