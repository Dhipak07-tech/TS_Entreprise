package com.connectit.core.employee.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserProvisionRequest {
    private Long employeeId;
    private String username;
    private String password;
    private Set<String> roles;
    private Long departmentId;
}
