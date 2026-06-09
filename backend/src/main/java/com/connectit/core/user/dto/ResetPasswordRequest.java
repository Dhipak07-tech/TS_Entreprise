package com.connectit.core.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
