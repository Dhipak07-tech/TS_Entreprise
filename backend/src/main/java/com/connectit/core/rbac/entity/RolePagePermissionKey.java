package com.connectit.core.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class RolePagePermissionKey implements Serializable {

    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "PAGE_ID")
    private Integer pageId;
}
