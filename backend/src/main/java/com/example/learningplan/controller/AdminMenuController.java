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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/menus")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuController {
    private static final String RECORD_NOT_FOUND = "Record not found";

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
        return loadMenus(startDate, endDate).stream()
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
        return buildTree(loadMenus(startDate, endDate), 0L);
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
        SysMenu menu = sysMenuRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
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
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        sysRoleMenuRepository.deleteByRoleId(role.getId());
        if (request.getMenuIds() != null) {
            for (Long menuId : request.getMenuIds()) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuRepository.save(roleMenu);
            }
        }
        return ResponseEntity.ok().build();
    }

    private List<SysMenu> loadMenus(String startDate, String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        return range == null
            ? sysMenuRepository.findAllByOrderBySortOrderAscIdAsc()
            : sysMenuRepository.findAllByCreatedAtBetweenOrderBySortOrderAscIdAsc(range.start, range.end);
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
            childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(menu);
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
}
