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
public class UserPagePermissionKey implements Serializable {

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "PAGE_ID")
    private Integer pageId;

    @Column(name = "ACTION_ID")
    private Long actionId;
}
