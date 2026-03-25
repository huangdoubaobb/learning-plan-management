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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String RECORD_NOT_FOUND = "Record not found";
    private static final String USERNAME_EXISTS = "Username already exists";
    private static final String PHONE_EXISTS = "Phone already exists";

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
            return ResponseEntity.badRequest().body(USERNAME_EXISTS);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest().body(PHONE_EXISTS);
        }
        Role role = roleRepository.findByCode("PARENT")
            .orElseThrow(() -> new IllegalStateException("PARENT role not found"));

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
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        List<String> roleCodes = loadRoleCodes(user.getId());
        if (roleCodes.isEmpty() && "admin".equalsIgnoreCase(user.getUsername())) {
            Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
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

        User user = userRepository.findById(principal.getUserId()).orElse(null);
        String phone = user == null ? "" : (user.getPhone() == null ? "" : user.getPhone());
        List<String> permissionCodes = loadPermissionCodes(principal.getUserId());
        List<String> roleCodes = principal.getRoleCodes();
        String primaryRole = roleCodes.isEmpty() ? "" : roleCodes.get(0);

        return new AuthProfileResponse(
            primaryRole,
            principal.getUserId(),
            principal.getDisplayName(),
            principal.getUsername(),
            phone,
            permissionCodes,
            user == null ? null : user.getLastLoginAt()
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                           @RequestBody AuthProfileUpdateRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        User user = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            user.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone)) {
                return ResponseEntity.badRequest().body(PHONE_EXISTS);
            }
            user.setPhone(newPhone);
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }
        userRepository.save(user);

        List<String> roleCodes = loadRoleCodes(user.getId());
        String primaryRole = roleCodes.isEmpty() ? "" : roleCodes.get(0);
        List<String> permissionCodes = loadPermissionCodes(principal.getUserId());
        return ResponseEntity.ok(new AuthProfileResponse(
            primaryRole,
            principal.getUserId(),
            user.getDisplayName(),
            user.getUsername(),
            user.getPhone(),
            permissionCodes,
            user.getLastLoginAt()
        ));
    }

    private void bindUserRole(Long userId, Long roleId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleRepository.save(userRole);
    }

    private List<String> loadPermissionCodes(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> menuIds = sysRoleMenuRepository.findByRoleIdIn(roleIds).stream()
            .map(rm -> rm.getMenuId())
            .distinct()
            .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        return sysMenuRepository.findByIdIn(menuIds).stream()
            .map(menu -> menu.getPermission())
            .filter(code -> code != null && !code.trim().isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }

    private List<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleRepository.findAllById(roleIds).stream()
            .sorted(Comparator.comparingInt(ControllerRoleSupport::rolePriority).thenComparing(Role::getCode))
            .map(Role::getCode)
            .collect(Collectors.toList());
    }
}
