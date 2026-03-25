package com.example.learningplan.controller;

import com.example.learningplan.dto.AdminUserCreateRequest;
import com.example.learningplan.dto.AdminUserUpdateRequest;
import com.example.learningplan.dto.PermissionView;
import com.example.learningplan.dto.RolePermissionsRequest;
import com.example.learningplan.dto.RoleRequest;
import com.example.learningplan.dto.RoleView;
import com.example.learningplan.dto.UserView;
import com.example.learningplan.entity.Role;
import com.example.learningplan.entity.SysMenu;
import com.example.learningplan.entity.SysRoleMenu;
import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.entity.User;
import com.example.learningplan.repository.RoleRepository;
import com.example.learningplan.repository.SysMenuRepository;
import com.example.learningplan.repository.SysRoleMenuRepository;
import com.example.learningplan.repository.SysUserRoleRepository;
import com.example.learningplan.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final String RECORD_NOT_FOUND = "Record not found";
    private static final String ROLE_NOT_FOUND = "Role not found";
    private static final String USERNAME_EXISTS = "Username already exists";

    private final RoleRepository roleRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final UserRepository userRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(RoleRepository roleRepository, SysMenuRepository sysMenuRepository,
                           SysRoleMenuRepository sysRoleMenuRepository, UserRepository userRepository,
                           SysUserRoleRepository sysUserRoleRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.sysMenuRepository = sysMenuRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
        this.userRepository = userRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/roles")
    public List<RoleView> listRoles(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<Role> roles = range == null
            ? roleRepository.findAll()
            : roleRepository.findByCreatedAtBetweenOrderByIdAsc(range.start, range.end);
        return roles.stream()
            .map(role -> {
                List<Long> menuIds = sysRoleMenuRepository.findByRoleId(role.getId()).stream()
                    .map(SysRoleMenu::getMenuId)
                    .collect(Collectors.toList());
                return new RoleView(role.getId(), role.getCode(), role.getName(), menuIds);
            })
            .collect(Collectors.toList());
    }

    @PostMapping("/roles")
    public Role createRole(@Valid @RequestBody RoleRequest request) {
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        return roleRepository.save(role);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        role.setCode(request.getCode());
        role.setName(request.getName());
        roleRepository.save(role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/permissions")
    public List<PermissionView> listPermissions(@RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<SysMenu> menus = range == null
            ? sysMenuRepository.findByPermissionIsNotNull()
            : sysMenuRepository.findByPermissionIsNotNullAndCreatedAtBetweenOrderBySortOrderAscIdAsc(range.start, range.end);
        return menus.stream()
            .map(menu -> new PermissionView(
                menu.getId(),
                menu.getPermission(),
                menu.getName(),
                null,
                null,
                null
            ))
            .collect(Collectors.toList());
    }

    @PutMapping("/roles/{id}/permissions")
    @Transactional
    public ResponseEntity<?> updateRolePermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionsRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        sysRoleMenuRepository.deleteByRoleId(role.getId());
        if (request.getPermissionIds() != null) {
            for (Long menuId : request.getPermissionIds()) {
                sysRoleMenuRepository.upsertRoleMenu(role.getId(), menuId);
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public List<UserView> listUsers(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<User> users = range == null
            ? userRepository.findAll()
            : userRepository.findByCreatedAtBetweenOrderByIdAsc(range.start, range.end);
        Map<Long, List<String>> roleCodesMap = buildUserRoleCodesMap(users);
        return users.stream()
            .map(user -> {
                List<String> roleCodes = roleCodesMap.getOrDefault(user.getId(), java.util.Collections.emptyList());
                String primaryRole = roleCodes.isEmpty() ? "" : roleCodes.get(0);
                return new UserView(
                    user.getId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    primaryRole,
                    roleCodes,
                    user.getPoints(),
                    user.getEnabled(),
                    user.getLastLoginAt()
                );
            })
            .collect(Collectors.toList());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(USERNAME_EXISTS);
        }
        Role role = roleRepository.findByCode(request.getRoleCode())
            .orElseThrow(() -> new IllegalStateException(ROLE_NOT_FOUND));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setPoints(request.getPoints() == null ? 0 : request.getPoints());
        user.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        userRepository.save(user);
        saveUserRole(user.getId(), role.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        Role role = roleRepository.findByCode(request.getRoleCode())
            .orElseThrow(() -> new IllegalStateException(ROLE_NOT_FOUND));

        user.setDisplayName(request.getDisplayName());
        if (request.getPoints() != null) {
            user.setPoints(request.getPoints());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }
        userRepository.save(user);
        saveUserRole(user.getId(), role.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private Map<Long, List<String>> buildUserRoleCodesMap(List<User> users) {
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, List<Long>> userRoles = sysUserRoleRepository.findByUserIdIn(userIds).stream()
            .collect(Collectors.groupingBy(
                SysUserRole::getUserId,
                Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())
            ));
        Map<Long, Role> roleMap = roleRepository.findAll().stream()
            .collect(Collectors.toMap(Role::getId, role -> role));

        return userRoles.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                .map(roleMap::get)
                .filter(role -> role != null)
                .sorted(Comparator.comparingInt(ControllerRoleSupport::rolePriority).thenComparing(Role::getCode))
                .map(Role::getCode)
                .collect(Collectors.toList())));
    }

    private void saveUserRole(Long userId, Long roleId) {
        sysUserRoleRepository.deleteByUserId(userId);
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleRepository.save(userRole);
    }
}
