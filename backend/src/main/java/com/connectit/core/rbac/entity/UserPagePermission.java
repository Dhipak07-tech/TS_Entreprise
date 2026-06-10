package com.connectit.core.rbac.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER_PAGE_PERMISSIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPagePermission {

    @EmbeddedId
    private UserPagePermissionKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pageId")
    @JoinColumn(name = "PAGE_ID")
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
    @JoinColumn(name = "ACTION_ID")
    private Action action;

    @Column(name = "IS_ALLOWED", nullable = false)
    @Builder.Default
    private Boolean isAllowed = true;
}
