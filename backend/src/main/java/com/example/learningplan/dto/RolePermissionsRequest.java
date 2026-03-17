package com.example.learningplan.dto;

import javax.validation.constraints.NotEmpty;

import java.util.List;

public class RolePermissionsRequest {
    @NotEmpty
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}

