package com.connectit.core.rbac.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPagePermissionDTO {
    private Integer pageId;
    private String actionCode;
    private Boolean isAllowed; // null means "inherit from role", true means "allow override", false means "deny override"
}
