package com.example.learningplan.dto;

public class UserView {
    private Long id;
    private String username;
    private String displayName;
    private String role;
    private java.util.List<String> roleCodes;
    private Integer points;
    private Boolean enabled;
    private java.time.LocalDateTime lastLoginAt;

    public UserView(Long id, String username, String displayName, String role, Integer points, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.roleCodes = null;
        this.points = points;
        this.enabled = enabled;
        this.lastLoginAt = null;
    }

    public UserView(Long id, String username, String displayName, String role, java.util.List<String> roleCodes,
                    Integer points, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.roleCodes = roleCodes;
        this.points = points;
        this.enabled = enabled;
        this.lastLoginAt = null;
    }

    public UserView(Long id, String username, String displayName, String role, java.util.List<String> roleCodes,
                    Integer points, Boolean enabled, java.time.LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.roleCodes = roleCodes;
        this.points = points;
        this.enabled = enabled;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public java.util.List<String> getRoleCodes() {
        return roleCodes;
    }

    public Integer getPoints() {
        return points;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public java.time.LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
