package com.example.learningplan.repository;

import com.example.learningplan.entity.SysOpLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysOpLogRepository extends JpaRepository<SysOpLog, Long> {
    List<SysOpLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    List<SysOpLog> findAllByOrderByCreatedAtDesc();
}
