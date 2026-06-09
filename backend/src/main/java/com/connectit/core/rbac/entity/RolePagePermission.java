package com.connectit.core.rbac.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROLE_PAGE_PERMISSIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePagePermission {

    @EmbeddedId
    private RolePagePermissionKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "ROLE_ID")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pageId")
    @JoinColumn(name = "PAGE_ID")
    private Page page;

    @Column(name = "CAN_VIEW", nullable = false)
    private Boolean canView = false;

    @Column(name = "CAN_CREATE", nullable = false)
    private Boolean canCreate = false;

    @Column(name = "CAN_UPDATE", nullable = false)
    private Boolean canUpdate = false;

    @Column(name = "CAN_DELETE", nullable = false)
    private Boolean canDelete = false;

    @Column(name = "CAN_APPROVE", nullable = false)
    private Boolean canApprove = false;

    @Column(name = "CAN_REJECT", nullable = false)
    private Boolean canReject = false;

    @Column(name = "CAN_ASSIGN", nullable = false)
    private Boolean canAssign = false;

    @Column(name = "CAN_IMPORT", nullable = false)
    private Boolean canImport = false;

    @Column(name = "CAN_EXPORT", nullable = false)
    private Boolean canExport = false;

    @Column(name = "CAN_PRINT", nullable = false)
    private Boolean canPrint = false;

    @Column(name = "CAN_REPORT_ACCESS", nullable = false)
    private Boolean canReportAccess = false;
}
