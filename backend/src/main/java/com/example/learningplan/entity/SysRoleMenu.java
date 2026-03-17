package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_role_menu")
@SQLDelete(sql = "UPDATE sys_role_menu SET is_deleted = 1 WHERE role_id = ? AND menu_id = ?")
@Where(clause = "is_deleted = 0")
@IdClass(SysRoleMenuId.class)
@Getter
@Setter
public class SysRoleMenu extends BaseEntity {
    @Id
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Id
    @Column(name = "menu_id", nullable = false)
    private Long menuId;
}
