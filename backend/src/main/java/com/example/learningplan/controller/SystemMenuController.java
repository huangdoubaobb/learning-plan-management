package com.example.learningplan.controller;

import com.example.learningplan.config.UserPrincipal;
import com.example.learningplan.dto.RouterMeta;
import com.example.learningplan.dto.RouterView;
import com.example.learningplan.entity.SysMenu;
import com.example.learningplan.entity.SysRoleMenu;
import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.repository.SysMenuRepository;
import com.example.learningplan.repository.SysRoleMenuRepository;
import com.example.learningplan.repository.SysUserRoleRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/menu")
@PreAuthorize("hasAnyRole('ADMIN','PARENT','CHILD')")
public class SystemMenuController {
    private final SysMenuRepository sysMenuRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;

    public SystemMenuController(SysMenuRepository sysMenuRepository,
                                SysUserRoleRepository sysUserRoleRepository,
                                SysRoleMenuRepository sysRoleMenuRepository) {
        this.sysMenuRepository = sysMenuRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
    }

    @GetMapping("/getRouters")
    public List<RouterView> getRouters(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return new ArrayList<>();
        }
        List<Long> roleIds = sysUserRoleRepository.findByUserId(principal.getUserId()).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> menuIds = sysRoleMenuRepository.findByRoleIdIn(roleIds).stream()
            .map(SysRoleMenu::getMenuId)
            .distinct()
            .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> allMenus = sysMenuRepository.findAllByOrderBySortOrderAscIdAsc();
        Set<Long> allowedIds = expandWithParents(menuIds, allMenus);
        List<SysMenu> allowedMenus = allMenus.stream()
            .filter(menu -> allowedIds.contains(menu.getId()))
            .filter(menu -> menu.getType() != null && (menu.getType() == 1 || menu.getType() == 2))
            .filter(menu -> !isTaskCreateMenu(menu))
            .collect(Collectors.toList());
        return buildRouterTree(allowedMenus, 0L);
    }

    private Set<Long> expandWithParents(List<Long> menuIds, List<SysMenu> allMenus) {
        Map<Long, SysMenu> menuMap = allMenus.stream()
            .collect(Collectors.toMap(SysMenu::getId, m -> m));
        Set<Long> allowed = new HashSet<>(menuIds);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Long id : new HashSet<>(allowed)) {
                SysMenu menu = menuMap.get(id);
                if (menu == null) continue;
                Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
                if (parentId != 0L && !allowed.contains(parentId)) {
                    allowed.add(parentId);
                    changed = true;
                }
            }
        }
        return allowed;
    }

    private List<RouterView> buildRouterTree(List<SysMenu> menus, Long rootId) {
        Map<Long, List<SysMenu>> childrenMap = new HashMap<>();
        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(menu);
        }
        return buildChildren(childrenMap, rootId);
    }

    private List<RouterView> buildChildren(Map<Long, List<SysMenu>> childrenMap, Long parentId) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, new ArrayList<>());
        children.sort(Comparator.comparing(SysMenu::getSortOrder).thenComparing(SysMenu::getId));
        List<RouterView> nodes = new ArrayList<>();
        for (SysMenu menu : children) {
            RouterMeta meta = new RouterMeta(
                menu.getName(),
                menu.getIcon(),
                menu.getCache(),
                menu.getPermission() == null ? null : Collections.singletonList(menu.getPermission())
            );
            RouterView node = new RouterView(
                menu.getPath(),
                menu.getComponent(),
                buildRouteName(menu),
                null,
                Boolean.FALSE.equals(menu.getVisible()),
                meta
            );
            node.getChildren().addAll(buildChildren(childrenMap, menu.getId()));
            if (node.getRedirect() == null && !node.getChildren().isEmpty()) {
                String firstPath = node.getChildren().get(0).getPath();
                if (firstPath != null && !firstPath.trim().isEmpty()) {
                    node.setRedirect(firstPath);
                }
            }
            nodes.add(node);
        }
        return nodes;
    }

    private String buildRouteName(SysMenu menu) {
        if (menu.getPermission() != null && !menu.getPermission().trim().isEmpty()) {
            return menu.getPermission().replace('.', '_');
        }
        return menu.getName();
    }

    private boolean isTaskCreateMenu(SysMenu menu) {
        if (menu == null) return false;
        String path = menu.getPath() == null ? "" : menu.getPath().trim().toLowerCase();
        String perm = menu.getPermission() == null ? "" : menu.getPermission().trim().toLowerCase();
        String name = menu.getName() == null ? "" : menu.getName().trim();
        if ("parent/task-create".equals(path) || "/parent/task-create".equals(path)) {
            return true;
        }
        if ("parent.task.create".equals(perm)) {
            return true;
        }
        return "任务创建".equals(name);
    }
}
