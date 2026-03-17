package com.example.learningplan.controller;

import com.example.learningplan.dto.RoleMenusRequest;
import com.example.learningplan.dto.SysMenuRequest;
import com.example.learningplan.dto.SysMenuTreeNode;
import com.example.learningplan.dto.SysMenuView;
import com.example.learningplan.entity.Role;
import com.example.learningplan.entity.SysMenu;
import com.example.learningplan.entity.SysRoleMenu;
import com.example.learningplan.repository.RoleRepository;
import com.example.learningplan.repository.SysMenuRepository;
import com.example.learningplan.repository.SysRoleMenuRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/menus")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuController {
    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final RoleRepository roleRepository;

    public AdminMenuController(SysMenuRepository sysMenuRepository, SysRoleMenuRepository sysRoleMenuRepository,
                               RoleRepository roleRepository) {
        this.sysMenuRepository = sysMenuRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public List<SysMenuView> listMenus(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        List<SysMenu> menus = range == null
            ? sysMenuRepository.findAllByOrderBySortOrderAscIdAsc()
            : sysMenuRepository.findAllByCreatedAtBetweenOrderBySortOrderAscIdAsc(range.start, range.end);
        return menus.stream()
            .map(menu -> new SysMenuView(
                menu.getId(),
                menu.getParentId(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getType(),
                menu.getPermission(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getCache(),
                menu.getIsFrame()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/tree")
    public List<SysMenuTreeNode> listMenuTree(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        List<SysMenu> menus = range == null
            ? sysMenuRepository.findAllByOrderBySortOrderAscIdAsc()
            : sysMenuRepository.findAllByCreatedAtBetweenOrderBySortOrderAscIdAsc(range.start, range.end);
        return buildTree(menus, 0L);
    }

    @PostMapping
    public SysMenu createMenu(@Valid @RequestBody SysMenuRequest request) {
        SysMenu menu = new SysMenu();
        applyRequest(menu, request);
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        return sysMenuRepository.save(menu);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @Valid @RequestBody SysMenuRequest request) {
        SysMenu menu = sysMenuRepository.findById(id).orElseThrow(() -> new IllegalStateException("Not found"));
        applyRequest(menu, request);
        sysMenuRepository.save(menu);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        sysMenuRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/role/{roleId}")
    public List<Long> getRoleMenus(@PathVariable Long roleId) {
        return sysRoleMenuRepository.findByRoleId(roleId).stream()
            .map(SysRoleMenu::getMenuId)
            .collect(Collectors.toList());
    }

    @PutMapping("/role/{roleId}")
    @Transactional
    public ResponseEntity<?> updateRoleMenus(@PathVariable Long roleId, @Valid @RequestBody RoleMenusRequest request) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalStateException("Not found"));
        sysRoleMenuRepository.deleteByRoleId(role.getId());
        if (request.getMenuIds() != null) {
            for (Long menuId : request.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                sysRoleMenuRepository.save(rm);
            }
        }
        return ResponseEntity.ok().build();
    }

    private void applyRequest(SysMenu menu, SysMenuRequest request) {
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setVisible(request.getVisible() == null ? Boolean.TRUE : request.getVisible());
        menu.setCache(request.getCache() == null ? Boolean.TRUE : request.getCache());
        menu.setIsFrame(request.getIsFrame() == null ? Boolean.FALSE : request.getIsFrame());
    }

    private List<SysMenuTreeNode> buildTree(List<SysMenu> menus, Long rootId) {
        Map<Long, List<SysMenu>> childrenMap = new HashMap<>();
        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(menu);
        }
        return buildChildren(childrenMap, rootId);
    }

    private List<SysMenuTreeNode> buildChildren(Map<Long, List<SysMenu>> childrenMap, Long parentId) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, new ArrayList<>());
        children.sort(Comparator.comparing(SysMenu::getSortOrder).thenComparing(SysMenu::getId));
        List<SysMenuTreeNode> nodes = new ArrayList<>();
        for (SysMenu menu : children) {
            SysMenuTreeNode node = new SysMenuTreeNode(
                menu.getId(),
                menu.getParentId(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getType(),
                menu.getPermission(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getCache(),
                menu.getIsFrame()
            );
            node.getChildren().addAll(buildChildren(childrenMap, menu.getId()));
            nodes.add(node);
        }
        return nodes;
    }

    private static class DateRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        static DateRange from(String startDate, String endDate) {
            if ((startDate == null || startDate.trim().isEmpty()) && (endDate == null || endDate.trim().isEmpty())) {
                return null;
            }
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDate.parse(startDate.trim()).atStartOfDay();
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDate.parse(endDate.trim()).atTime(LocalTime.MAX);
            }
            if (start == null) {
                start = LocalDate.of(2000, 1, 1).atStartOfDay();
            }
            if (end == null) {
                end = LocalDateTime.now();
            }
            return new DateRange(start, end);
        }
    }
}
