package com.example.learningplan.repository;

import com.example.learningplan.entity.ParentChild;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentChildRepository extends JpaRepository<ParentChild, Long> {
    List<ParentChild> findByParentId(Long parentId);
    List<ParentChild> findByParentIdAndCreatedAtBetween(Long parentId, LocalDateTime start, LocalDateTime end);
    List<ParentChild> findByChildId(Long childId);
    boolean existsByParentIdAndChildId(Long parentId, Long childId);
    long countByParentId(Long parentId);
}
