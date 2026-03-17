package com.example.learningplan.dto;

import java.util.List;

public class RouterMeta {
    private String title;
    private String icon;
    private Boolean cache;
    private List<String> permissions;

    public RouterMeta() {}

    public RouterMeta(String title, String icon, Boolean cache, List<String> permissions) {
        this.title = title;
        this.icon = icon;
        this.cache = cache;
        this.permissions = permissions;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public Boolean getCache() {
        return cache;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
