package com.connectit.core.rbac.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePagePermissionDTO {
    private Long roleId;
    private Integer pageId;
    private String pageName;
    private Boolean canView;
    private Boolean canCreate;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canApprove;
    private Boolean canReject;
    private Boolean canAssign;
    private Boolean canImport;
    private Boolean canExport;
    private Boolean canPrint;
    private Boolean canReportAccess;
}
