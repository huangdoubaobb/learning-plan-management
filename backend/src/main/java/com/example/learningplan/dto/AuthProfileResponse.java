package com.example.learningplan.dto;

import java.util.List;

public class AuthProfileResponse {
    private String role;
    private Long userId;
    private String displayName;
    private String username;
    private String phone;
    private List<String> permissions;
    private java.time.LocalDateTime lastLoginAt;

    public AuthProfileResponse(String role, Long userId, String displayName, List<String> permissions) {
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
        this.permissions = permissions;
        this.lastLoginAt = null;
    }

    public AuthProfileResponse(String role, Long userId, String displayName, String username, List<String> permissions) {
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.permissions = permissions;
        this.lastLoginAt = null;
    }

    public AuthProfileResponse(String role, Long userId, String displayName, String username, String phone, List<String> permissions) {
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.phone = phone;
        this.permissions = permissions;
        this.lastLoginAt = null;
    }

    public AuthProfileResponse(String role, Long userId, String displayName, String username, String phone,
                               List<String> permissions, java.time.LocalDateTime lastLoginAt) {
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.phone = phone;
        this.permissions = permissions;
        this.lastLoginAt = lastLoginAt;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public java.time.LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
