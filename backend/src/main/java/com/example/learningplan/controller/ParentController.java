package com.example.learningplan.controller;

import com.example.learningplan.config.UserPrincipal;
import com.example.learningplan.dto.*;
import com.example.learningplan.dto.RedemptionParentView;
import com.example.learningplan.entity.*;
import com.example.learningplan.repository.*;
import com.example.learningplan.service.PointsService;
import com.example.learningplan.service.ReportService;
import com.example.learningplan.dto.export.PointsTrendRow;
import com.example.learningplan.dto.export.SummaryRow;
import com.example.learningplan.dto.export.TasksTrendRow;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.UUID;

@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasRole('PARENT')")
public class ParentController {
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final ParentChildRepository parentChildRepository;
    private final TaskRepository taskRepository;
    private final RewardRepository rewardRepository;
    private final RedemptionRepository redemptionRepository;
    private final ReportService reportService;
    private final PointsService pointsService;
    private final PasswordEncoder passwordEncoder;

    public ParentController(UserRepository userRepository, RoleRepository roleRepository,
                            SysUserRoleRepository sysUserRoleRepository,
                            ParentChildRepository parentChildRepository, TaskRepository taskRepository,
                            RewardRepository rewardRepository, RedemptionRepository redemptionRepository,
                            ReportService reportService, PointsService pointsService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.parentChildRepository = parentChildRepository;
        this.taskRepository = taskRepository;
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.reportService = reportService;
        this.pointsService = pointsService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal UserPrincipal principal) {
        User parent = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Not found"));
        return new UserView(parent.getId(), parent.getUsername(), parent.getDisplayName(),
            primaryRoleCode(parent.getId()), parent.getPoints(), parent.getEnabled());
    }

    @PostMapping("/children")
    public ResponseEntity<?> createChild(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody ChildCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        Role childRole = roleRepository.findByCode("CHILD")
            .orElseThrow(() -> new IllegalStateException("CHILD 角色不存在"));
        User child = new User();
        child.setUsername(request.getUsername());
        child.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        child.setDisplayName(request.getDisplayName());
        userRepository.save(child);
        saveUserRole(child.getId(), childRole.getId());

        ParentChild mapping = new ParentChild();
        mapping.setParent(userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Not found")));
        mapping.setChild(child);
        parentChildRepository.save(mapping);

        return ResponseEntity.ok(new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
            primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled()));
    }

    @GetMapping("/children")
    public List<UserView> listChildren(@AuthenticationPrincipal UserPrincipal principal,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        List<ParentChild> mappings = range == null
            ? parentChildRepository.findByParentId(principal.getUserId())
            : parentChildRepository.findByParentIdAndCreatedAtBetween(principal.getUserId(), range.start, range.end);
        return mappings.stream()
            .map(ParentChild::getChild)
            .map(child -> new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
                primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled()))
            .collect(Collectors.toList());
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody TaskCreateRequest request) {
        if (!parentChildRepository.existsByParentIdAndChildId(principal.getUserId(), request.getChildId())) {
            return ResponseEntity.badRequest().body("孩子不属于当前家长");
        }
        User parent = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Not found"));
        User child = userRepository.findById(request.getChildId())
            .orElseThrow(() -> new IllegalStateException("Not found"));

        Task task = new Task();
        task.setParent(parent);
        task.setChild(child);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPoints(request.getPoints());
        taskRepository.save(task);

        return ResponseEntity.ok(toTaskView(task));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody TaskCreateRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前家长");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("已完成任务不允许修改");
        }
        if (task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body("任务已提交审核，不能修改");
        }
        if (!parentChildRepository.existsByParentIdAndChildId(principal.getUserId(), request.getChildId())) {
            return ResponseEntity.badRequest().body("孩子不属于当前家长");
        }
        User child = userRepository.findById(request.getChildId())
            .orElseThrow(() -> new IllegalStateException("Not found"));

        task.setChild(child);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPoints(request.getPoints());
        taskRepository.save(task);

        return ResponseEntity.ok(toTaskView(task));
    }

    @PostMapping("/tasks/{id}/images")
    @Transactional
    public ResponseEntity<?> uploadTaskImages(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable Long id,
                                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                                              @RequestParam(value = "replace", defaultValue = "false") boolean replace) throws IOException {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("Task does not belong to current parent");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body("Task cannot be modified");
        }
        if (files == null || files.length == 0) {
            return ResponseEntity.ok(toTaskView(task));
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String error = validateImage(file);
            if (error != null) {
                return ResponseEntity.badRequest().body(error);
            }
            String url = storeTaskImage(task.getId(), file);
            if (!url.isEmpty()) {
                urls.add(url);
            }
        }
        if (replace) {
            task.setTaskImages(String.join(",", urls));
        } else {
            List<String> existing = new ArrayList<>(parseImages(task.getTaskImages()));
            existing.addAll(urls);
            task.setTaskImages(String.join(",", existing));
        }
        taskRepository.save(task);
        return ResponseEntity.ok(toTaskView(task));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前家长");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body("已完成任务不允许删除");
        }
        taskRepository.delete(task);
        return ResponseEntity.ok("删除成功");
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTask(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前家长");
        }
        return ResponseEntity.ok(toTaskView(task));
    }

    @GetMapping("/tasks")
    public List<TaskView> getTasks(@AuthenticationPrincipal UserPrincipal principal) {
        List<Task> tasks = taskRepository.findByParentId(principal.getUserId());
        return tasks.stream()
            .map(this::toTaskView)
            .collect(Collectors.toList());
    }

    @GetMapping("/task-stats")
    public ResponseEntity<?> getTaskStats(@AuthenticationPrincipal UserPrincipal principal) {
        long totalTasks = taskRepository.countByParentId(principal.getUserId());
        long pendingTasks = taskRepository.countSearchTasksByCreatedAt(principal.getUserId(), null, TaskStatus.PENDING, null, null, null);
        long completedTasks = taskRepository.countSearchTasksByCreatedAt(principal.getUserId(), null, TaskStatus.COMPLETED, null, null, null);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", totalTasks);
        stats.put("pending", pendingTasks);
        stats.put("completed", completedTasks);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/rewards")
    public List<RewardView> getRewards(@AuthenticationPrincipal UserPrincipal principal) {
        List<Reward> rewards = rewardRepository.findByParentId(principal.getUserId());
        return rewards.stream()
            .map(this::toRewardView)
            .collect(Collectors.toList());
    }

    @GetMapping("/rewards/{id}")
    public ResponseEntity<?> getReward(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!reward.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("Reward does not belong to current parent");
        }
        return ResponseEntity.ok(toRewardView(reward));
    }

    @PostMapping("/rewards")
    public ResponseEntity<?> createReward(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody RewardCreateRequest request) {
        User parent = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Not found"));
        Reward reward = new Reward();
        reward.setParent(parent);
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        reward.setPointsCost(request.getPointsCost());
        reward.setStock(request.getStock());
        rewardRepository.save(reward);
        return ResponseEntity.ok(toRewardView(reward));
    }

    @PutMapping("/rewards/{id}")
    public ResponseEntity<?> updateReward(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id,
                                          @Valid @RequestBody RewardCreateRequest request) {
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!reward.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("Reward does not belong to current parent");
        }
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        reward.setPointsCost(request.getPointsCost());
        reward.setStock(request.getStock());
        rewardRepository.save(reward);
        return ResponseEntity.ok(toRewardView(reward));
    }

    @DeleteMapping("/rewards/{id}")
    public ResponseEntity<?> deleteReward(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id) {
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!reward.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("Reward does not belong to current parent");
        }
        rewardRepository.delete(reward);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/rewards/{id}/images")
    @Transactional
    public ResponseEntity<?> uploadRewardImages(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id,
                                                @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!reward.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("Reward does not belong to current parent");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.ok(toRewardView(reward));
        }
        String error = validateImage(file);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        String url = storeRewardImage(reward.getId(), file);
        reward.setImages(url);
        rewardRepository.save(reward);
        return ResponseEntity.ok(toRewardView(reward));
    }

    @GetMapping("/redemptions")
    public List<RedemptionParentView> getRedemptions(@AuthenticationPrincipal UserPrincipal principal) {
        List<Redemption> redemptions = redemptionRepository.findByRewardParentId(principal.getUserId());
        return redemptions.stream()
            .map(redemption -> new RedemptionParentView(
                redemption.getId(),
                redemption.getReward().getId(),
                redemption.getReward().getName(),
                redemption.getChild().getId(),
                redemption.getChild().getDisplayName(),
                redemption.getPointsCost(),
                redemption.getStatus(),
                redemption.getRedeemedAt(),
                redemption.getReviewNote(),
                redemption.getReviewedAt()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/tasks/page")
    public ResponseEntity<?> getTasksPage(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Long childId,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        TaskStatus taskStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                taskStatus = TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效的状态值
            }
        }
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage = taskRepository.searchTasksByCreatedAt(
            principal.getUserId(),
            childId,
            taskStatus,
            range != null ? range.start : null,
            range != null ? range.end : null,
            q,
            pageRequest
        );
        
        List<TaskView> taskViews = taskPage.getContent().stream()
            .map(this::toTaskView)
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", taskViews);
        response.put("total", taskPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveTask(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前家长");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("任务已完成");
        }
        if (task.getSubmittedAt() == null) {
            return ResponseEntity.badRequest().body("任务尚未提交审核");
        }
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);

        User child = task.getChild();
        pointsService.earnPoints(child, task.getPoints(), PointsRefType.TASK, task.getId(), "任务完成");

        return ResponseEntity.ok("审核通过");
    }

    @PostMapping("/tasks/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectTask(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getParent().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前家长");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("任务已完成");
        }
        task.setSubmittedAt(null);
        task.setCompletedAt(null);
        task.setCheckinNote(null);
        task.setCheckinImages(null);
        taskRepository.save(task);
        return ResponseEntity.ok("已拒绝");
    }

    @PostMapping("/redemptions/batch-approve")
    @Transactional
    public ResponseEntity<?> approveRedemptions(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestBody RedemptionBatchRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return ResponseEntity.badRequest().body("请选择记录");
        }
        for (Long id : request.getIds()) {
            Redemption redemption = redemptionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Not found"));
            if (!redemption.getReward().getParent().getId().equals(principal.getUserId())) {
                return ResponseEntity.badRequest().body("兑换记录不属于当前家长");
            }
            if (redemption.getStatus() != RedemptionStatus.PENDING) {
                continue;
            }
            redemption.setStatus(RedemptionStatus.APPROVED);
            redemption.setReviewedAt(LocalDateTime.now());
            if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
                redemption.setReviewNote(request.getNote().trim());
            }
            redemptionRepository.save(redemption);
        }
        return ResponseEntity.ok("批量已同意");
    }

    @PostMapping("/redemptions/batch-reject")
    @Transactional
    public ResponseEntity<?> rejectRedemptions(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestBody RedemptionBatchRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return ResponseEntity.badRequest().body("请选择记录");
        }
        for (Long id : request.getIds()) {
            Redemption redemption = redemptionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Not found"));
            if (!redemption.getReward().getParent().getId().equals(principal.getUserId())) {
                return ResponseEntity.badRequest().body("兑换记录不属于当前家长");
            }
            if (redemption.getStatus() != RedemptionStatus.PENDING) {
                continue;
            }
            Reward reward = redemption.getReward();
            reward.setStock(reward.getStock() + 1);
            rewardRepository.save(reward);

            User child = redemption.getChild();
            String note = "兑换被拒绝返还积分";
            if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
                note = note + " - " + request.getNote().trim();
                redemption.setReviewNote(request.getNote().trim());
            }
            pointsService.earnPoints(child, redemption.getPointsCost(), PointsRefType.REWARD, reward.getId(), note);

            redemption.setStatus(RedemptionStatus.REJECTED);
            redemption.setReviewedAt(LocalDateTime.now());
            redemptionRepository.save(redemption);
        }
        return ResponseEntity.ok("批量已拒绝");
    }

    @GetMapping("/stats")
    public ParentStatsResponse stats(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam(defaultValue = "30") int days) {
        long childrenTotal = parentChildRepository.countByParentId(principal.getUserId());
        long tasksTotal = taskRepository.countByParentId(principal.getUserId());
        long rewardsTotal = rewardRepository.countByParentId(principal.getUserId());
        long pending = redemptionRepository.countByRewardParentIdAndStatus(principal.getUserId(), RedemptionStatus.PENDING);

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(Math.max(days, 1));
        long tasksInRange = taskRepository.countByParentIdAndCreatedAtBetween(principal.getUserId(), start, end);
        long redemptionsInRange = redemptionRepository.countByRewardParentIdAndRedeemedAtBetween(principal.getUserId(), start, end);

        return new ParentStatsResponse(childrenTotal, tasksTotal, rewardsTotal, pending, tasksInRange, redemptionsInRange);
    }

    @GetMapping("/tasks/counts")
    public ResponseEntity<?> getTaskCounts(@AuthenticationPrincipal UserPrincipal principal,
                                           @RequestParam(required = false) Long childId,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        LocalDateTime start = range != null ? range.start : null;
        LocalDateTime end = range != null ? range.end : null;
        long pendingCount = taskRepository.countSearchTasksByCreatedAt(principal.getUserId(), childId, TaskStatus.PENDING, start, end, q);
        long completedCount = taskRepository.countSearchTasksByCreatedAt(principal.getUserId(), childId, TaskStatus.COMPLETED, start, end, q);
        
        Map<String, Long> counts = new HashMap<>();
        counts.put("pending", pendingCount);
        counts.put("completed", completedCount);
        
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/report/summary")
    public ResponseEntity<?> reportSummary(@AuthenticationPrincipal UserPrincipal principal,
                                           @RequestParam Long childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(principal.getUserId(), childId)) {
            return ResponseEntity.badRequest().body("孩子不属于当前家长");
        }
        return ResponseEntity.ok(reportService.buildSummary(childId));
    }

    @GetMapping("/report/trend")
    public ResponseEntity<?> reportTrend(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestParam Long childId,
                                         @RequestParam(defaultValue = "14") int days) {
        if (!parentChildRepository.existsByParentIdAndChildId(principal.getUserId(), childId)) {
            return ResponseEntity.badRequest().body("孩子不属于当前家长");
        }
        return ResponseEntity.ok(reportService.buildTrend(childId, days));
    }

    @GetMapping("/report/points-trend")
    public ResponseEntity<?> reportPointsTrend(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam Long childId,
                                               @RequestParam(defaultValue = "14") int days) {
        if (!parentChildRepository.existsByParentIdAndChildId(principal.getUserId(), childId)) {
            return ResponseEntity.badRequest().body("孩子不属于当前家长");
        }
        return ResponseEntity.ok(reportService.buildPointsTrend(childId, days));
    }

    @GetMapping("/report/export")
    public ResponseEntity<byte[]> exportReport(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam Long childId,
                                               @RequestParam(defaultValue = "30") int days) throws Exception {
        if (!parentChildRepository.existsByParentIdAndChildId(principal.getUserId(), childId)) {
            return ResponseEntity.badRequest().build();
        }
        ReportSummaryResponse summary = reportService.buildSummary(childId);
        ReportTrendResponse trend = reportService.buildTrend(childId, days);
        ReportPointsTrendResponse pointsTrend = reportService.buildPointsTrend(childId, days);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter writer = EasyExcel.write(out).build();
        try {
            List<SummaryRow> summaryData = new java.util.ArrayList<>();
            summaryData.add(new SummaryRow("任务总数", String.valueOf(summary.getTasksTotal())));
            summaryData.add(new SummaryRow("已完成", String.valueOf(summary.getTasksCompleted())));
            double rate = summary.getTasksTotal() == 0 ? 0 : (double) summary.getTasksCompleted() / (double) summary.getTasksTotal();
            summaryData.add(new SummaryRow("完成率", String.format("%.2f%%", rate * 100)));
            summaryData.add(new SummaryRow("当前积分", String.valueOf(summary.getPointsBalance())));
            summaryData.add(new SummaryRow("积分获取", String.valueOf(summary.getPointsEarned())));
            summaryData.add(new SummaryRow("积分消耗", String.valueOf(summary.getPointsSpent())));
            summaryData.add(new SummaryRow("兑换次数", String.valueOf(summary.getRewardsRedeemed())));

            List<TasksTrendRow> trendData = new java.util.ArrayList<>();
            for (ReportTrendPoint point : trend.getPoints()) {
                trendData.add(new TasksTrendRow(
                    point.getDate(),
                    point.getTasksTotal(),
                    point.getTasksCompleted(),
                    String.format("%.2f%%", point.getCompletionRate() * 100)
                ));
            }

            List<PointsTrendRow> pointsData = new java.util.ArrayList<>();
            for (ReportPointsTrendPoint point : pointsTrend.getPoints()) {
                pointsData.add(new PointsTrendRow(
                    point.getDate(),
                    point.getPointsEarned(),
                    point.getPointsSpent()
                ));
            }

            WriteSheet summarySheet = EasyExcel.writerSheet("Summary")
                .head(SummaryRow.class)
                .build();
            writer.write(summaryData, summarySheet);

            WriteSheet trendSheet = EasyExcel.writerSheet("TasksTrend")
                .head(TasksTrendRow.class)
                .build();
            writer.write(trendData, trendSheet);

            WriteSheet pointsSheet = EasyExcel.writerSheet("PointsTrend")
                .head(PointsTrendRow.class)
                .build();
            writer.write(pointsData, pointsSheet);
        } finally {
            writer.finish();
        }

        byte[] bytes = out.toByteArray();
        String filename = "report-" + childId + "-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    private TaskView toTaskView(Task task) {
        List<String> taskImages = parseImages(task.getTaskImages());
        List<String> checkinImages = parseImages(task.getCheckinImages());
        List<String> images = mergeImages(taskImages, checkinImages);
        return new TaskView(task.getId(), task.getChild().getId(), task.getTitle(), task.getDescription(), task.getPoints(), task.getStatus(),
            task.getSubmittedAt(), task.getCompletedAt(), task.getCheckinNote(), task.getCreatedAt(), images, taskImages, checkinImages);
    }

    private RewardView toRewardView(Reward reward) {
        List<String> images = parseImages(reward.getImages());
        return new RewardView(reward.getId(), reward.getName(), reward.getDescription(), reward.getPointsCost(), reward.getStock(), reward.getStatus(), images);
    }

    private List<String> parseImages(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private List<String> mergeImages(List<String> taskImages, List<String> checkinImages) {
        List<String> merged = new ArrayList<>();
        if (taskImages != null) merged.addAll(taskImages);
        if (checkinImages != null) merged.addAll(checkinImages);
        return merged;
    }

    private String validateImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "No file provided";
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            return "Only JPG/PNG images are allowed";
        }
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();
            if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
                return "Only JPG/PNG images are allowed";
            }
        }
        if (!isImageMagic(file)) {
            return "Invalid image format";
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            return "Image size cannot exceed 10MB";
        }
        return null;
    }

    private String storeTaskImage(Long taskId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "";
        }
        Path root = Paths.get(uploadDir, "task-attachments", String.valueOf(taskId));
        Files.createDirectories(root);
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = root.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return "/uploads/task-attachments/" + taskId + "/" + filename;
    }

    private String storeRewardImage(Long rewardId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "";
        }
        Path root = Paths.get(uploadDir, "reward-images", String.valueOf(rewardId));
        Files.createDirectories(root);
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = root.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return "/uploads/reward-images/" + rewardId + "/" + filename;
    }

    private boolean isImageMagic(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = in.read(header);
            if (read < 3) return false;
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }
            if (read >= 8 &&
                (header[0] & 0xFF) == 0x89 &&
                header[1] == 0x50 &&
                header[2] == 0x4E &&
                header[3] == 0x47 &&
                header[4] == 0x0D &&
                header[5] == 0x0A &&
                header[6] == 0x1A &&
                header[7] == 0x0A) {
                return true;
            }
            return false;
        }
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String key;
        if ("points".equalsIgnoreCase(sortBy)) key = "points";
        else if ("status".equalsIgnoreCase(sortBy)) key = "status";
        else if ("completedAt".equalsIgnoreCase(sortBy)) key = "completedAt";
        else if ("createdAt".equalsIgnoreCase(sortBy)) key = "createdAt";
        else if ("title".equalsIgnoreCase(sortBy)) key = "title";
        else key = "createdAt";

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, key);
    }

    private void saveUserRole(Long userId, Long roleId) {
        sysUserRoleRepository.deleteByUserId(userId);
        sysUserRoleRepository.upsertUserRole(userId, roleId);
    }

    private String primaryRoleCode(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return "";
        }
        List<Role> roles = roleRepository.findAllById(roleIds).stream()
            .sorted(Comparator.comparingInt(this::rolePriority).thenComparing(Role::getCode))
            .collect(Collectors.toList());
        return roles.isEmpty() ? "" : roles.get(0).getCode();
    }

    private int rolePriority(Role role) {
        if (role == null || role.getCode() == null) return 99;
        switch (role.getCode()) {
            case "ADMIN":
                return 0;
            case "PARENT":
                return 1;
            case "CHILD":
                return 2;
            default:
                return 50;
        }
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


