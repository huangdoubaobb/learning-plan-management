package com.example.learningplan.repository;

import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.entity.SysUserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, SysUserRoleId> {
    List<SysUserRole> findByUserId(Long userId);
    List<SysUserRole> findByUserIdIn(List<Long> userIds);

    @Modifying
    @Transactional
    @Query("update SysUserRole ur set ur.isDeleted = true, ur.updatedAt = CURRENT_TIMESTAMP where ur.userId = ?1")
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    @Query(value = "insert into sys_user_role (user_id, role_id, created_at, updated_at, is_deleted) " +
        "values (?1, ?2, now(), now(), 0) " +
        "on duplicate key update is_deleted = 0, updated_at = now()", nativeQuery = true)
    void upsertUserRole(Long userId, Long roleId);
}
