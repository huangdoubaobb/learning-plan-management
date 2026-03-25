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
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.UUID;

@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasRole('PARENT')")
public class ParentController {
    private static final String RECORD_NOT_FOUND = "Record not found";
    private static final String ACCESS_DENIED = "Access denied";
    private static final String CHILD_NOT_BOUND = "Child does not belong to the current parent";
    private static final String TASK_LOCKED = "Task cannot be changed after submission or completion";

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final ParentChildRepository parentChildRepository;
    private final TaskRepository taskRepository;
    private final RewardRepository rewardRepository;
    private final RedemptionRepository redemptionRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final ReportService reportService;
    private final PointsService pointsService;
    private final PasswordEncoder passwordEncoder;

    public ParentController(UserRepository userRepository, RoleRepository roleRepository,
                             SysUserRoleRepository sysUserRoleRepository,
                             ParentChildRepository parentChildRepository, TaskRepository taskRepository,
                             RewardRepository rewardRepository, RedemptionRepository redemptionRepository,
                             PointsLedgerRepository pointsLedgerRepository,
                             ReportService reportService, PointsService pointsService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.parentChildRepository = parentChildRepository;
        this.taskRepository = taskRepository;
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.pointsLedgerRepository = pointsLedgerRepository;
        this.reportService = reportService;
        this.pointsService = pointsService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal UserPrincipal principal) {
        User parent = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        return new UserView(parent.getId(), parent.getUsername(), parent.getDisplayName(),
            primaryRoleCode(parent.getId()), parent.getPoints(), parent.getEnabled());
    }

    @PostMapping("/children")
    public ResponseEntity<?> createChild(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody ChildCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        Role childRole = roleRepository.findByCode("CHILD")
            .orElseThrow(() -> new IllegalStateException("Role not found"));
        User child = new User();
        child.setUsername(request.getUsername());
        child.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        child.setDisplayName(request.getDisplayName());
        userRepository.save(child);
        saveUserRole(child.getId(), childRole.getId());

        ParentChild mapping = new ParentChild();
        mapping.setParent(userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND)));
        mapping.setChild(child);
        parentChildRepository.save(mapping);

        return ResponseEntity.ok(new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
            primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled()));
    }

    @GetMapping("/children")
    public List<UserView> listChildren(@AuthenticationPrincipal UserPrincipal principal,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<ParentChild> mappings = range == null
            ? parentChildRepository.findByParentId(principal.getUserId())
            : parentChildRepository.findByParentIdAndCreatedAtBetween(principal.getUserId(), range.start, range.end);
        return mappings.stream()
            .map(ParentChild::getChild)
            .map(child -> new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
                primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled()))
            .collect(Collectors.toList());
    }

    @PutMapping("/children/{id}")
    public ResponseEntity<?> updateChild(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long id,
                                         @Valid @RequestBody ChildUpdateRequest request) {
        User child = findOwnedChild(principal.getUserId(), id);
        if (child == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        String nextUsername = request.getUsername().trim();
        if (!child.getUsername().equals(nextUsername) && userRepository.existsByUsername(nextUsername)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        child.setUsername(nextUsername);
        child.setDisplayName(request.getDisplayName().trim());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            child.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }
        userRepository.save(child);
        return ResponseEntity.ok(new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
            primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled()));
    }

    @DeleteMapping("/children/{id}")
    @Transactional
    public ResponseEntity<?> deleteChild(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long id) {
        User child = findOwnedChild(principal.getUserId(), id);
        if (child == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (!taskRepository.findByChildId(child.getId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Child cannot be deleted because task records exist");
        }
        if (!redemptionRepository.findByChildId(child.getId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Child cannot be deleted because redemption records exist");
        }
        if (!pointsServiceRecordsEmpty(child.getId())) {
            return ResponseEntity.badRequest().body("Child cannot be deleted because points records exist");
        }
        parentChildRepository.findByChildId(child.getId()).forEach(parentChildRepository::delete);
        sysUserRoleRepository.deleteByUserId(child.getId());
        userRepository.delete(child);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody TaskCreateRequest request) {
        ResponseEntity<?> childCheck = validateChildOwnership(principal.getUserId(), request.getChildId());
        if (childCheck != null) {
            return childCheck;
        }
        User parent = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        User child = userRepository.findById(request.getChildId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));

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
        Task task = findParentTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Completed task cannot be edited");
        }
        if (task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body("Submitted task cannot be edited");
        }
        ResponseEntity<?> childCheck = validateChildOwnership(principal.getUserId(), request.getChildId());
        if (childCheck != null) {
            return childCheck;
        }
        User child = userRepository.findById(request.getChildId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));

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
        Task task = findParentTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body(TASK_LOCKED);
        }
        if (files == null || files.length == 0) {
            return ResponseEntity.ok(toTaskView(task));
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String error = ControllerImageSupport.validateRequiredImage(file);
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
            List<String> existing = new ArrayList<>(ControllerImageSupport.parseImages(task.getTaskImages()));
            existing.addAll(urls);
            task.setTaskImages(String.join(",", existing));
        }
        taskRepository.save(task);
        return ResponseEntity.ok(toTaskView(task));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
        Task task = findParentTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body(TASK_LOCKED);
        }
        taskRepository.delete(task);
        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTask(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id) {
        Task task = findParentTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
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
        Reward reward = findParentReward(principal.getUserId(), id);
        if (reward == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        return ResponseEntity.ok(toRewardView(reward));
    }

    @PostMapping("/rewards")
    public ResponseEntity<?> createReward(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody RewardCreateRequest request) {
        User parent = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
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
        Reward reward = findParentReward(principal.getUserId(), id);
        if (reward == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
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
        Reward reward = findParentReward(principal.getUserId(), id);
        if (reward == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        rewardRepository.delete(reward);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PostMapping("/rewards/{id}/images")
    @Transactional
    public ResponseEntity<?> uploadRewardImages(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id,
                                                @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        Reward reward = findParentReward(principal.getUserId(), id);
        if (reward == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.ok(toRewardView(reward));
        }
        String error = ControllerImageSupport.validateRequiredImage(file);
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
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        TaskStatus taskStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                taskStatus = TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                taskStatus = null;
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
        Task task = findParentTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Task already approved");
        }
        if (task.getSubmittedAt() == null) {
            return ResponseEntity.badRequest().body("Task has not been submitted");
        }
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);

        User child = task.getChild();
        pointsService.earnPoints(child, task.getPoints(), PointsRefType.TASK, task.getId(), "Task approved");

        return ResponseEntity.ok("Approved successfully");
    }

    @PostMapping("/tasks/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectTask(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Task task = findParentTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Completed task cannot be rejected");
        }
        task.setSubmittedAt(null);
        task.setCompletedAt(null);
        task.setCheckinNote(null);
        task.setCheckinImages(null);
        taskRepository.save(task);
        return ResponseEntity.ok("Rejected successfully");
    }

    @PostMapping("/redemptions/batch-approve")
    @Transactional
    public ResponseEntity<?> approveRedemptions(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestBody RedemptionBatchRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Please select at least one redemption");
        }
        for (Long id : request.getIds()) {
            Redemption redemption = findParentRedemption(principal.getUserId(), id);
            if (redemption == null) {
                return ResponseEntity.badRequest().body(ACCESS_DENIED);
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
        return ResponseEntity.ok("Approved successfully");
    }

    @PostMapping("/redemptions/batch-reject")
    @Transactional
    public ResponseEntity<?> rejectRedemptions(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestBody RedemptionBatchRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Please select at least one redemption");
        }
        for (Long id : request.getIds()) {
            Redemption redemption = findParentRedemption(principal.getUserId(), id);
            if (redemption == null) {
                return ResponseEntity.badRequest().body(ACCESS_DENIED);
            }
            if (redemption.getStatus() != RedemptionStatus.PENDING) {
                continue;
            }
            Reward reward = redemption.getReward();
            reward.setStock(reward.getStock() + 1);
            rewardRepository.save(reward);

            User child = redemption.getChild();
            String note = "Reward redemption rejected";
            if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
                note = note + " - " + request.getNote().trim();
                redemption.setReviewNote(request.getNote().trim());
            }
            pointsService.earnPoints(child, redemption.getPointsCost(), PointsRefType.REWARD, reward.getId(), note);

            redemption.setStatus(RedemptionStatus.REJECTED);
            redemption.setReviewedAt(LocalDateTime.now());
            redemptionRepository.save(redemption);
        }
        return ResponseEntity.ok("Rejected successfully");
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
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
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
        ResponseEntity<?> childCheck = validateChildOwnership(principal.getUserId(), childId);
        if (childCheck != null) {
            return childCheck;
        }
        return ResponseEntity.ok(reportService.buildSummary(childId));
    }

    @GetMapping("/report/trend")
    public ResponseEntity<?> reportTrend(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestParam Long childId,
                                         @RequestParam(defaultValue = "14") int days) {
        ResponseEntity<?> childCheck = validateChildOwnership(principal.getUserId(), childId);
        if (childCheck != null) {
            return childCheck;
        }
        return ResponseEntity.ok(reportService.buildTrend(childId, days));
    }

    @GetMapping("/report/points-trend")
    public ResponseEntity<?> reportPointsTrend(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam Long childId,
                                               @RequestParam(defaultValue = "14") int days) {
        ResponseEntity<?> childCheck = validateChildOwnership(principal.getUserId(), childId);
        if (childCheck != null) {
            return childCheck;
        }
        return ResponseEntity.ok(reportService.buildPointsTrend(childId, days));
    }

    @GetMapping("/report/export")
    public ResponseEntity<byte[]> exportReport(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam Long childId,
                                               @RequestParam(defaultValue = "30") int days) throws Exception {
        if (validateChildOwnership(principal.getUserId(), childId) != null) {
            return ResponseEntity.badRequest().build();
        }
        ReportSummaryResponse summary = reportService.buildSummary(childId);
        ReportTrendResponse trend = reportService.buildTrend(childId, days);
        ReportPointsTrendResponse pointsTrend = reportService.buildPointsTrend(childId, days);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter writer = EasyExcel.write(out).build();
        try {
            List<SummaryRow> summaryData = new java.util.ArrayList<>();
            summaryData.add(new SummaryRow("Total Tasks", String.valueOf(summary.getTasksTotal())));
            summaryData.add(new SummaryRow("Completed Tasks", String.valueOf(summary.getTasksCompleted())));
            double rate = summary.getTasksTotal() == 0 ? 0 : (double) summary.getTasksCompleted() / (double) summary.getTasksTotal();
            summaryData.add(new SummaryRow("Completion Rate", String.format("%.2f%%", rate * 100)));
            summaryData.add(new SummaryRow("Points Balance", String.valueOf(summary.getPointsBalance())));
            summaryData.add(new SummaryRow("Points Earned", String.valueOf(summary.getPointsEarned())));
            summaryData.add(new SummaryRow("Points Spent", String.valueOf(summary.getPointsSpent())));
            summaryData.add(new SummaryRow("Rewards Redeemed", String.valueOf(summary.getRewardsRedeemed())));

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
        List<String> taskImages = ControllerImageSupport.parseImages(task.getTaskImages());
        List<String> checkinImages = ControllerImageSupport.parseImages(task.getCheckinImages());
        List<String> images = ControllerImageSupport.mergeImages(taskImages, checkinImages);
        return new TaskView(task.getId(), task.getChild().getId(), task.getTitle(), task.getDescription(), task.getPoints(), task.getStatus(),
            task.getSubmittedAt(), task.getCompletedAt(), task.getCheckinNote(), task.getCreatedAt(), images, taskImages, checkinImages);
    }

    private RewardView toRewardView(Reward reward) {
        List<String> images = ControllerImageSupport.parseImages(reward.getImages());
        return new RewardView(
            reward.getId(),
            reward.getName(),
            reward.getDescription(),
            reward.getPointsCost(),
            reward.getStock(),
            reward.getStatus(),
            reward.getCreatedAt(),
            images
        );
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

    private void saveUserRole(Long userId, Long roleId) {
        sysUserRoleRepository.deleteByUserId(userId);
        sysUserRoleRepository.upsertUserRole(userId, roleId);
    }

    private ResponseEntity<?> validateChildOwnership(Long parentId, Long childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            return ResponseEntity.badRequest().body(CHILD_NOT_BOUND);
        }
        return null;
    }

    private Task findParentTask(Long parentId, Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        if (!task.getParent().getId().equals(parentId)) {
            return null;
        }
        return task;
    }

    private Reward findParentReward(Long parentId, Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        if (!reward.getParent().getId().equals(parentId)) {
            return null;
        }
        return reward;
    }

    private Redemption findParentRedemption(Long parentId, Long redemptionId) {
        Redemption redemption = redemptionRepository.findById(redemptionId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        if (!redemption.getReward().getParent().getId().equals(parentId)) {
            return null;
        }
        return redemption;
    }

    private User findOwnedChild(Long parentId, Long childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            return null;
        }
        return userRepository.findById(childId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
    }

    private boolean pointsServiceRecordsEmpty(Long childId) {
        return pointsLedgerRepository.findByChildId(childId).isEmpty();
    }

    private String primaryRoleCode(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return "";
        }
        List<Role> roles = roleRepository.findAllById(roleIds).stream()
            .sorted(Comparator.comparingInt(ControllerRoleSupport::rolePriority).thenComparing(Role::getCode))
            .collect(Collectors.toList());
        return roles.isEmpty() ? "" : roles.get(0).getCode();
    }

}
