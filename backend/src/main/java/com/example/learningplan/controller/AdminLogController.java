package com.example.learningplan.controller;

import com.example.learningplan.entity.SysOpLog;
import com.example.learningplan.repository.SysOpLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {
    private final SysOpLogRepository sysOpLogRepository;

    public AdminLogController(SysOpLogRepository sysOpLogRepository) {
        this.sysOpLogRepository = sysOpLogRepository;
    }

    @GetMapping
    public List<SysOpLog> listLogs(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        if (range == null) {
            return sysOpLogRepository.findAllByOrderByCreatedAtDesc();
        }
        return sysOpLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(range.start, range.end);
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
