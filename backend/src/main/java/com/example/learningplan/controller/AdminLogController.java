package com.example.learningplan.controller;

import com.example.learningplan.entity.SysOpLog;
import com.example.learningplan.repository.SysOpLogRepository;
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
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        if (range == null) {
            return sysOpLogRepository.findAllByOrderByCreatedAtDesc();
        }
        return sysOpLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(range.start, range.end);
    }
}
