package com.connectit.core.rbac.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PAGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @Column(name = "PAGE_ID")
    private Integer pageId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "PATH", nullable = false, length = 255)
    private String path;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}
