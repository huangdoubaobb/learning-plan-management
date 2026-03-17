package com.example.learningplan.controller;

import com.example.learningplan.config.JwtService;
import com.example.learningplan.config.UserPrincipal;
import com.example.learningplan.dto.AuthLoginRequest;
import com.example.learningplan.dto.AuthProfileResponse;
import com.example.learningplan.dto.AuthProfileUpdateRequest;
import com.example.learningplan.dto.AuthRegisterParentRequest;
import com.example.learningplan.dto.AuthResponse;
import com.example.learningplan.entity.Role;
import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.entity.User;
import com.example.learningplan.repository.RoleRepository;
import com.example.learningplan.repository.SysMenuRepository;
import com.example.learningplan.repository.SysRoleMenuRepository;
import com.example.learningplan.repository.SysUserRoleRepository;
import com.example.learningplan.repository.UserRepository;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository,
                          SysRoleMenuRepository sysRoleMenuRepository, SysMenuRepository sysMenuRepository,
                          SysUserRoleRepository sysUserRoleRepository,
                          PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
        this.sysMenuRepository = sysMenuRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register-parent")
    public ResponseEntity<?> registerParent(@Valid @RequestBody AuthRegisterParentRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest().body("手机号已存在");
        }
        Role role = roleRepository.findByCode("PARENT")
            .orElseThrow(() -> new IllegalStateException("PARENT 角色不存在"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        bindUserRole(user.getId(), role.getId());

        String token = jwtService.generateToken(user, role.getCode());
        return ResponseEntity.ok(new AuthResponse(token, role.getCode(), user.getId(), user.getDisplayName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsernameOrPhone(request.getUsername(), request.getUsername())
            .orElseThrow(() -> new IllegalStateException("用户不存在"));
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        List<String> roleCodes = loadRoleCodes(user.getId());
        if (roleCodes.isEmpty() && "admin".equalsIgnoreCase(user.getUsername())) {
            Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN 角色不存在"));
            bindUserRole(user.getId(), adminRole.getId());
            roleCodes = java.util.Arrays.asList("ADMIN");
        }
        String primaryRole = roleCodes.isEmpty() ? "" : roleCodes.get(0);
        String token = jwtService.generateToken(user, primaryRole);
        return ResponseEntity.ok(new AuthResponse(token, primaryRole, user.getId(), user.getDisplayName()));
    }

    @GetMapping("/me")
    public AuthProfileResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return new AuthProfileResponse("", null, "", "", "", Collections.emptyList(), null);
        }
        List<Long> roleIds = sysUserRoleRepository.findByUserId(principal.getUserId()).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        List<String> permissionCodes;
        if (roleIds.isEmpty()) {
            permissionCodes = Collections.emptyList();
        } else {
            List<Long> menuIds = sysRoleMenuRepository.findByRoleIdIn(roleIds).stream()
                .map(rm -> rm.getMenuId())
                .distinct()
                .collect(Collectors.toList());
            if (menuIds.isEmpty()) {
                permissionCodes = Collections.emptyList();
            } else {
                permissionCodes = sysMenuRepository.findByIdIn(menuIds).stream()
                    .map(menu -> menu.getPermission())
                    .filter(code -> code != null && !code.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            }
        }
        List<String> roleCodes = principal.getRoleCodes();
        String primaryRole = roleCodes.isEmpty() ? "" : roleCodes.get(0);
        User user = userRepository.findById(principal.getUserId()).orElse(null);
        String phone = user == null ? "" : (user.getPhone() == null ? "" : user.getPhone());
        return new AuthProfileResponse(primaryRole, principal.getUserId(), principal.getDisplayName(),
            principal.getUsername(), phone, permissionCodes, user == null ? null : user.getLastLoginAt());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                           @RequestBody AuthProfileUpdateRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("用户不存在"));
        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            user.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone)) {
                return ResponseEntity.badRequest().body("手机号已存在");
            }
            user.setPhone(newPhone);
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }
        userRepository.save(user);
        List<String> roleCodes = loadRoleCodes(user.getId());
        String primaryRole = roleCodes.isEmpty() ? "" : roleCodes.get(0);
        List<Long> roleIds = sysUserRoleRepository.findByUserId(principal.getUserId()).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        List<String> permissionCodes;
        if (roleIds.isEmpty()) {
            permissionCodes = Collections.emptyList();
        } else {
            List<Long> menuIds = sysRoleMenuRepository.findByRoleIdIn(roleIds).stream()
                .map(rm -> rm.getMenuId())
                .distinct()
                .collect(Collectors.toList());
            if (menuIds.isEmpty()) {
                permissionCodes = Collections.emptyList();
            } else {
                permissionCodes = sysMenuRepository.findByIdIn(menuIds).stream()
                    .map(menu -> menu.getPermission())
                    .filter(code -> code != null && !code.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            }
        }
        return ResponseEntity.ok(new AuthProfileResponse(primaryRole, principal.getUserId(),
            user.getDisplayName(), user.getUsername(), user.getPhone(), permissionCodes, user.getLastLoginAt()));
    }

    private void bindUserRole(Long userId, Long roleId) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleRepository.save(ur);
    }

    private List<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleRepository.findAllById(roleIds).stream()
            .sorted(Comparator.comparingInt(this::rolePriority).thenComparing(Role::getCode))
            .map(Role::getCode)
            .collect(Collectors.toList());
    }

    private int rolePriority(Role role) {
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
