package com.example.learningplan.repository;

import com.example.learningplan.entity.PointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
    List<PointsLedger> findByChildId(Long childId);
    List<PointsLedger> findByChildIdAndCreatedAtBetween(Long childId, LocalDateTime start, LocalDateTime end);
}
