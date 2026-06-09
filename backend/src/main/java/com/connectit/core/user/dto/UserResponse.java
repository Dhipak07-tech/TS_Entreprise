package com.connectit.core.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Boolean isActive;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private String preferredLanguage;
    private String departmentName;
    private Set<String> roles;
}
