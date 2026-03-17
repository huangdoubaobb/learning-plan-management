package com.example.learningplan.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SysMenuRequest {
    @NotBlank
    private String name;

    @NotNull
    private Integer type;

    private Long parentId;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Boolean visible;
    private Boolean cache;
    private Boolean isFrame;

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getPath() {
        return path;
    }

    public String getComponent() {
        return component;
    }

    public String getPermission() {
        return permission;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getVisible() {
        return visible;
    }

    public Boolean getCache() {
        return cache;
    }

    public Boolean getIsFrame() {
        return isFrame;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public void setIsFrame(Boolean isFrame) {
        this.isFrame = isFrame;
    }
}

