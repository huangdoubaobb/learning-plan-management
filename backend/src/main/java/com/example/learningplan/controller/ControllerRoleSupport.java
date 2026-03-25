package com.example.learningplan.controller;

import com.example.learningplan.entity.Role;

final class ControllerRoleSupport {
    private ControllerRoleSupport() {}

    static int rolePriority(Role role) {
        if (role == null || role.getCode() == null) {
            return 99;
        }
        switch (role.getCode()) {
            case "ADMIN":
                return 0;
            case "PARENT":
                return 1;
            case "CHILD":
                return 2;
            default:
                return 50;
        }
    }
}
