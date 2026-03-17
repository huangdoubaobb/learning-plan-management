package com.example.learningplan.repository;

import com.example.learningplan.entity.Redemption;
import com.example.learningplan.entity.RedemptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    List<Redemption> findByChildId(Long childId);
    List<Redemption> findByChildIdAndCreatedAtBetween(Long childId, LocalDateTime start, LocalDateTime end);
    List<Redemption> findByRewardParentId(Long parentId);
    List<Redemption> findByRewardParentIdAndCreatedAtBetween(Long parentId, LocalDateTime start, LocalDateTime end);
    long countByRewardParentIdAndStatus(Long parentId, RedemptionStatus status);
    long countByRewardParentIdAndRedeemedAtBetween(Long parentId, LocalDateTime start, LocalDateTime end);
}
