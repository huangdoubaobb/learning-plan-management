package com.example.learningplan.config;

import com.example.learningplan.entity.Role;
import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.entity.User;
import com.example.learningplan.repository.RoleRepository;
import com.example.learningplan.repository.SysUserRoleRepository;
import com.example.learningplan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public AdminBootstrap(UserRepository userRepository, RoleRepository roleRepository,
                          SysUserRoleRepository sysUserRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        Role adminRole = roleRepository.findByCode("ADMIN")
            .orElseThrow(() -> new IllegalStateException("ADMIN 角色不存在"));
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setDisplayName("系统管理员");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        userRepository.save(admin);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(admin.getId());
        ur.setRoleId(adminRole.getId());
        sysUserRoleRepository.save(ur);
    }
}
