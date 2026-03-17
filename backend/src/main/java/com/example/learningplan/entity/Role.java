package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "roles")
@SQLDelete(sql = "UPDATE roles SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    // 角色与菜单权限关系改为 sys_role_menu 维护
}

