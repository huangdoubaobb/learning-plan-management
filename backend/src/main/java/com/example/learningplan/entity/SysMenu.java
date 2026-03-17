package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_menu")
@SQLDelete(sql = "UPDATE sys_menu SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
public class SysMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    @Column(nullable = false)
    private String name;

    private String path;

    private String component;

    @Column(nullable = false)
    private Integer type;

    private String permission;

    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(nullable = false)
    private Boolean cache = true;

    @Column(name = "is_frame", nullable = false)
    private Boolean isFrame = false;
}
