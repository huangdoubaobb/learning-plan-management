package com.example.learningplan.config;

import com.example.learningplan.entity.Role;
import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.entity.User;
import com.example.learningplan.repository.RoleRepository;
import com.example.learningplan.repository.SysUserRoleRepository;
import com.example.learningplan.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    SysUserRoleRepository sysUserRoleRepository,
                                    RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrPhone(username, username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Long> roleIds = sysUserRoleRepository.findByUserId(user.getId()).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        List<String> roleCodes = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            roleCodes = roleRepository.findAllById(roleIds).stream()
                .sorted(Comparator.comparingInt(this::rolePriority).thenComparing(Role::getCode))
                .map(Role::getCode)
                .collect(Collectors.toList());
        }
        return new UserPrincipal(user, roleCodes);
    }

    private int rolePriority(Role role) {
        if (role == null || role.getCode() == null) return 99;
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
