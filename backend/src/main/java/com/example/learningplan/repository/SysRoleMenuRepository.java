package com.example.learningplan.repository;

import com.example.learningplan.entity.SysRoleMenu;
import com.example.learningplan.entity.SysRoleMenuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, SysRoleMenuId> {
    List<SysRoleMenu> findByRoleId(Long roleId);
    List<SysRoleMenu> findByRoleIdIn(List<Long> roleIds);

    @Modifying
    @Transactional
    @Query("update SysRoleMenu rm set rm.isDeleted = true, rm.updatedAt = CURRENT_TIMESTAMP where rm.roleId = ?1")
    void deleteByRoleId(Long roleId);

    @Modifying
    @Transactional
    @Query(value = "insert into sys_role_menu (role_id, menu_id, created_at, updated_at, is_deleted) " +
        "values (?1, ?2, now(), now(), 0) " +
        "on duplicate key update is_deleted = 0, updated_at = now()", nativeQuery = true)
    void upsertRoleMenu(Long roleId, Long menuId);
}
