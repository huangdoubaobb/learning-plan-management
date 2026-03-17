package com.example.learningplan.dto;

import java.util.ArrayList;
import java.util.List;

public class RouterView {
    private String path;
    private String component;
    private String name;
    private String redirect;
    private Boolean hidden;
    private RouterMeta meta;
    private List<RouterView> children = new ArrayList<>();

    public RouterView() {}

    public RouterView(String path, String component, String name, String redirect, Boolean hidden, RouterMeta meta) {
        this.path = path;
        this.component = component;
        this.name = name;
        this.redirect = redirect;
        this.hidden = hidden;
        this.meta = meta;
    }

    public String getPath() {
        return path;
    }

    public String getComponent() {
        return component;
    }

    public String getName() {
        return name;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public RouterMeta getMeta() {
        return meta;
    }

    public List<RouterView> getChildren() {
        return children;
    }
}
