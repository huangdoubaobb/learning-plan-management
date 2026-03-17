package com.example.learningplan.dto;

public class AuthResponse {
    private String token;
    private String role;
    private Long userId;
    private String displayName;

    public AuthResponse(String token, String role, Long userId, String displayName) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
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
}
