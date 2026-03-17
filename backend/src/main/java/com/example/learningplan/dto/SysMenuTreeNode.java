package com.example.learningplan.dto;

import java.util.ArrayList;
import java.util.List;

public class SysMenuTreeNode {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private Integer type;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Boolean visible;
    private Boolean cache;
    private Boolean isFrame;
    private List<SysMenuTreeNode> children = new ArrayList<>();

    public SysMenuTreeNode(Long id, Long parentId, String name, String path, String component, Integer type,
                           String permission, String icon, Integer sortOrder, Boolean visible, Boolean cache, Boolean isFrame) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.path = path;
        this.component = component;
        this.type = type;
        this.permission = permission;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.visible = visible;
        this.cache = cache;
        this.isFrame = isFrame;
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getComponent() {
        return component;
    }

    public Integer getType() {
        return type;
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

    public List<SysMenuTreeNode> getChildren() {
        return children;
    }
}

