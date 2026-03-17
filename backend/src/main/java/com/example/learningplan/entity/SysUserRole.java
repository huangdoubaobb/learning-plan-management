package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_user_role")
@SQLDelete(sql = "UPDATE sys_user_role SET is_deleted = 1 WHERE user_id = ? AND role_id = ?")
@Where(clause = "is_deleted = 0")
@IdClass(SysUserRoleId.class)
@Getter
@Setter
public class SysUserRole extends BaseEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "role_id", nullable = false)
    private Long roleId;
}
