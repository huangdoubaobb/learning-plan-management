package com.example.learningplan.repository;

import com.example.learningplan.entity.SysMenu;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {
    List<SysMenu> findAllByOrderBySortOrderAscIdAsc();
    List<SysMenu> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);
    List<SysMenu> findByIdIn(List<Long> ids);
    List<SysMenu> findByPermissionIsNotNull();
    List<SysMenu> findAllByCreatedAtBetweenOrderBySortOrderAscIdAsc(LocalDateTime start, LocalDateTime end);
    List<SysMenu> findByPermissionIsNotNullAndCreatedAtBetweenOrderBySortOrderAscIdAsc(LocalDateTime start, LocalDateTime end);
}
