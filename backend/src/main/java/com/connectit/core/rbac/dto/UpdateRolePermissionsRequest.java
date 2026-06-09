package com.connectit.core.rbac.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateRolePermissionsRequest {
    private List<String> permissionKeys;
}
