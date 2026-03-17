package com.example.learningplan.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RoleMenusRequest {
    @NotNull
    private List<Long> menuIds;

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}

