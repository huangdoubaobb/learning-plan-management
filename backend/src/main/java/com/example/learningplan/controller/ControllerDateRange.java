package com.example.learningplan.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

final class ControllerDateRange {
    final LocalDateTime start;
    final LocalDateTime end;

    private ControllerDateRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    static ControllerDateRange from(String startDate, String endDate) {
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
        return new ControllerDateRange(start, end);
    }
}
