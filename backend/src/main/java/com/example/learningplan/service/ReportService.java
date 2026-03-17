package com.example.learningplan.service;

import com.example.learningplan.dto.ReportPointsTrendPoint;
import com.example.learningplan.dto.ReportPointsTrendResponse;
import com.example.learningplan.dto.ReportSummaryResponse;
import com.example.learningplan.dto.ReportTrendPoint;
import com.example.learningplan.dto.ReportTrendResponse;
import com.example.learningplan.entity.PointsLedger;
import com.example.learningplan.entity.PointsType;
import com.example.learningplan.entity.Task;
import com.example.learningplan.entity.TaskStatus;
import com.example.learningplan.repository.PointsLedgerRepository;
import com.example.learningplan.repository.RedemptionRepository;
import com.example.learningplan.repository.TaskRepository;
import com.example.learningplan.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final TaskRepository taskRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final RedemptionRepository redemptionRepository;
    private final UserRepository userRepository;

    public ReportService(TaskRepository taskRepository, PointsLedgerRepository pointsLedgerRepository,
                         RedemptionRepository redemptionRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.pointsLedgerRepository = pointsLedgerRepository;
        this.redemptionRepository = redemptionRepository;
        this.userRepository = userRepository;
    }

    public ReportSummaryResponse buildSummary(Long childId) {
        long total = taskRepository.countByChildId(childId);
        long completed = taskRepository.countByChildIdAndStatus(childId, TaskStatus.COMPLETED);
        int balance = userRepository.findById(childId).map(u -> u.getPoints()).orElse(0);
        int earned = pointsLedgerRepository.findByChildId(childId).stream()
            .filter(p -> p.getType() == PointsType.EARN)
            .mapToInt(p -> p.getPoints())
            .sum();
        int spent = pointsLedgerRepository.findByChildId(childId).stream()
            .filter(p -> p.getType() == PointsType.SPEND)
            .mapToInt(p -> p.getPoints())
            .sum();
        long redeemed = redemptionRepository.findByChildId(childId).size();
        return new ReportSummaryResponse(childId, total, completed, balance, earned, spent, redeemed);
    }

    public ReportTrendResponse buildTrend(Long childId, int days) {
        if (days <= 0) {
            days = 7;
        }
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<Task> createdTasks = taskRepository.findByChildIdAndCreatedAtBetween(childId, startTime, endTime);
        List<Task> completedTasks = taskRepository.findByChildIdAndCompletedAtBetween(childId, startTime, endTime);

        Map<LocalDate, Long> totalsByDate = new HashMap<>();
        for (Task task : createdTasks) {
            LocalDate date = task.getCreatedAt().toLocalDate();
            totalsByDate.put(date, totalsByDate.getOrDefault(date, 0L) + 1);
        }

        Map<LocalDate, Long> completedByDate = new HashMap<>();
        for (Task task : completedTasks) {
            if (task.getCompletedAt() == null) {
                continue;
            }
            LocalDate date = task.getCompletedAt().toLocalDate();
            completedByDate.put(date, completedByDate.getOrDefault(date, 0L) + 1);
        }

        List<ReportTrendPoint> points = new ArrayList<>();
        long totalAll = 0;
        long completedAll = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            long total = totalsByDate.getOrDefault(date, 0L);
            long completed = completedByDate.getOrDefault(date, 0L);
            double rate = total == 0 ? 0 : (double) completed / (double) total;
            points.add(new ReportTrendPoint(date.toString(), total, completed, rate));
            totalAll += total;
            completedAll += completed;
        }

        double completionRate = totalAll == 0 ? 0 : (double) completedAll / (double) totalAll;
        return new ReportTrendResponse(childId, completionRate, points);
    }

    public ReportPointsTrendResponse buildPointsTrend(Long childId, int days) {
        if (days <= 0) {
            days = 7;
        }
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<PointsLedger> ledgers = pointsLedgerRepository.findByChildIdAndCreatedAtBetween(childId, startTime, endTime);
        Map<LocalDate, Integer> earnedByDate = new HashMap<>();
        Map<LocalDate, Integer> spentByDate = new HashMap<>();
        ledgers.forEach(ledger -> {
            LocalDate date = ledger.getCreatedAt().toLocalDate();
            if (ledger.getType() == PointsType.EARN) {
                earnedByDate.put(date, earnedByDate.getOrDefault(date, 0) + ledger.getPoints());
            } else {
                spentByDate.put(date, spentByDate.getOrDefault(date, 0) + ledger.getPoints());
            }
        });

        List<ReportPointsTrendPoint> points = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            int earned = earnedByDate.getOrDefault(date, 0);
            int spent = spentByDate.getOrDefault(date, 0);
            points.add(new ReportPointsTrendPoint(date.toString(), earned, spent));
        }

        int balance = userRepository.findById(childId).map(u -> u.getPoints()).orElse(0);
        return new ReportPointsTrendResponse(childId, balance, points);
    }
}
