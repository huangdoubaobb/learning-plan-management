package com.example.learningplan.entity;

import java.io.Serializable;
import java.util.Objects;

public class SysRoleMenuId implements Serializable {
    private Long roleId;
    private Long menuId;

    public SysRoleMenuId() {}

    public SysRoleMenuId(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SysRoleMenuId)) return false;
        SysRoleMenuId that = (SysRoleMenuId) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, menuId);
    }
}

