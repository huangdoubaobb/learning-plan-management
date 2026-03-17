package com.example.learningplan.entity;

import java.io.Serializable;
import java.util.Objects;

public class SysUserRoleId implements Serializable {
    private Long userId;
    private Long roleId;

    public SysUserRoleId() {}

    public SysUserRoleId(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SysUserRoleId)) return false;
        SysUserRoleId that = (SysUserRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
