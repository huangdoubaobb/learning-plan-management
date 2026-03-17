package com.example.learningplan.config;

import com.example.learningplan.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private final User user;
    private final List<String> roleCodes;

    public UserPrincipal(User user, List<String> roleCodes) {
        this.user = user;
        if (roleCodes == null || roleCodes.isEmpty()) {
            this.roleCodes = Collections.emptyList();
        } else {
            this.roleCodes = roleCodes;
        }
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getRoleCode() {
        if (roleCodes.isEmpty()) {
            return "";
        }
        return roleCodes.get(0);
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public String getDisplayName() {
        return user.getDisplayName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roleCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return roleCodes.stream()
            .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
            .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getEnabled());
    }
}
