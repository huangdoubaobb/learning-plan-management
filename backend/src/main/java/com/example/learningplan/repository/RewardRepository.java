package com.example.learningplan.repository;

import com.example.learningplan.entity.Reward;
import com.example.learningplan.entity.RewardStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByParentId(Long parentId);
    List<Reward> findByParentIdAndCreatedAtBetween(Long parentId, LocalDateTime start, LocalDateTime end);
    List<Reward> findByParentIdAndStatus(Long parentId, RewardStatus status);
    List<Reward> findByParentIdAndStatusAndCreatedAtBetween(Long parentId, RewardStatus status, LocalDateTime start, LocalDateTime end);
    long countByParentId(Long parentId);
}
