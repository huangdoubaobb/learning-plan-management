package com.example.learningplan.dto;

import java.util.List;

public class RoleView {
    private Long id;
    private String code;
    private String name;
    private List<Long> permissionIds;

    public RoleView(Long id, String code, String name, List<Long> permissionIds) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.permissionIds = permissionIds;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }
}
