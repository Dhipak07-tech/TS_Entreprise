package com.connectit.core.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private Long departmentId;

    private Long teamLeadId;
}
