package com.connectit.core.employee.dto;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long departmentId;
    private String jobTitle;
    private String status;
    private Long managerId;
}
