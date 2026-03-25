package com.example.learningplan.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.learningplan.config.UserPrincipal;
import com.example.learningplan.dto.RedemptionView;
import com.example.learningplan.dto.RewardView;
import com.example.learningplan.dto.TaskCompleteRequest;
import com.example.learningplan.dto.TaskView;
import com.example.learningplan.dto.UserView;
import com.example.learningplan.dto.export.ChildPointsRow;
import com.example.learningplan.dto.export.SummaryRow;
import com.example.learningplan.entity.ParentChild;
import com.example.learningplan.entity.PointsLedger;
import com.example.learningplan.entity.PointsRefType;
import com.example.learningplan.entity.PointsType;
import com.example.learningplan.entity.Redemption;
import com.example.learningplan.entity.RedemptionStatus;
import com.example.learningplan.entity.Reward;
import com.example.learningplan.entity.RewardStatus;
import com.example.learningplan.entity.Role;
import com.example.learningplan.entity.SysUserRole;
import com.example.learningplan.entity.Task;
import com.example.learningplan.entity.TaskStatus;
import com.example.learningplan.entity.User;
import com.example.learningplan.repository.ParentChildRepository;
import com.example.learningplan.repository.PointsLedgerRepository;
import com.example.learningplan.repository.RedemptionRepository;
import com.example.learningplan.repository.RewardRepository;
import com.example.learningplan.repository.RoleRepository;
import com.example.learningplan.repository.SysUserRoleRepository;
import com.example.learningplan.repository.TaskRepository;
import com.example.learningplan.repository.UserRepository;
import com.example.learningplan.service.PointsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/child")
@PreAuthorize("hasRole('CHILD')")
public class ChildController {
    private static final String RECORD_NOT_FOUND = "Record not found";
    private static final String ACCESS_DENIED = "Access denied";

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final TaskRepository taskRepository;
    private final RewardRepository rewardRepository;
    private final RedemptionRepository redemptionRepository;
    private final UserRepository userRepository;
    private final ParentChildRepository parentChildRepository;
    private final PointsService pointsService;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final RoleRepository roleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    public ChildController(TaskRepository taskRepository, RewardRepository rewardRepository,
                           RedemptionRepository redemptionRepository, UserRepository userRepository,
                           ParentChildRepository parentChildRepository, PointsService pointsService,
                           PointsLedgerRepository pointsLedgerRepository, RoleRepository roleRepository,
                           SysUserRoleRepository sysUserRoleRepository) {
        this.taskRepository = taskRepository;
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.userRepository = userRepository;
        this.parentChildRepository = parentChildRepository;
        this.pointsService = pointsService;
        this.pointsLedgerRepository = pointsLedgerRepository;
        this.roleRepository = roleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
    }

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal UserPrincipal principal) {
        User child = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        return new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
            primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled());
    }

    @GetMapping("/tasks")
    public List<TaskView> listTasks(@AuthenticationPrincipal UserPrincipal principal,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<Task> tasks = range == null
            ? taskRepository.findByChildId(principal.getUserId())
            : taskRepository.findByChildIdAndCreatedAtBetween(principal.getUserId(), range.start, range.end);
        return tasks.stream()
            .map(this::toTaskView)
            .collect(Collectors.toList());
    }

    @PostMapping("/tasks/{id}/submit")
    @Transactional
    public ResponseEntity<?> submitTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @RequestParam(value = "note", required = false) String note,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        Task task = findChildTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Task already completed");
        }
        if (task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body("Task already submitted");
        }
        String error = ControllerImageSupport.validateOptionalImage(file);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        List<String> imageUrls = storeTaskImages(task.getId(), file);
        if (note != null && !note.trim().isEmpty()) {
            task.setCheckinNote(note.trim());
        }
        task.setCheckinImages(String.join(",", imageUrls));
        task.setSubmittedAt(LocalDateTime.now());
        taskRepository.save(task);
        return ResponseEntity.ok("Submitted successfully");
    }

    @PostMapping("/tasks/{id}/complete")
    @Transactional
    public ResponseEntity<?> completeTask(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id,
                                          @RequestBody(required = false) TaskCompleteRequest request) {
        Task task = findChildTask(principal.getUserId(), id);
        if (task == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.ok("Task already completed");
        }
        if (task.getSubmittedAt() != null) {
            return ResponseEntity.ok("Task already submitted");
        }
        if (request != null && request.getNote() != null && !request.getNote().trim().isEmpty()) {
            task.setCheckinNote(request.getNote().trim());
        }
        task.setSubmittedAt(LocalDateTime.now());
        taskRepository.save(task);

        return ResponseEntity.ok("Submitted successfully");
    }

    @GetMapping("/rewards")
    public List<RewardView> listRewards(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate) {
        Optional<Long> parentId = findParentId(principal.getUserId());
        if (!parentId.isPresent()) {
            return Collections.emptyList();
        }
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<Reward> rewards = range == null
            ? rewardRepository.findByParentIdAndStatus(parentId.get(), RewardStatus.ACTIVE)
            : rewardRepository.findByParentIdAndStatusAndCreatedAtBetween(parentId.get(), RewardStatus.ACTIVE, range.start, range.end);
        return rewards.stream()
            .map(reward -> new RewardView(
                reward.getId(),
                reward.getName(),
                reward.getDescription(),
                reward.getPointsCost(),
                reward.getStock(),
                reward.getStatus(),
                reward.getCreatedAt(),
                ControllerImageSupport.parseImages(reward.getImages())
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/rewards/{id}/redeem")
    @Transactional
    public ResponseEntity<?> redeemReward(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Optional<Long> parentId = findParentId(principal.getUserId());
        Reward reward = findFamilyReward(parentId, id);
        if (reward == null) {
            return ResponseEntity.badRequest().body(ACCESS_DENIED);
        }
        if (reward.getStatus() != RewardStatus.ACTIVE) {
            return ResponseEntity.badRequest().body("Reward cannot be redeemed");
        }
        if (reward.getStock() <= 0) {
            return ResponseEntity.badRequest().body("Reward is out of stock");
        }
        User child = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        pointsService.spendPoints(child, reward.getPointsCost(), PointsRefType.REWARD, reward.getId(), "Redeem reward");

        reward.setStock(reward.getStock() - 1);
        rewardRepository.save(reward);

        Redemption redemption = new Redemption();
        redemption.setReward(reward);
        redemption.setChild(child);
        redemption.setPointsCost(reward.getPointsCost());
        redemption.setStatus(RedemptionStatus.PENDING);
        redemptionRepository.save(redemption);

        return ResponseEntity.ok("Redemption request submitted");
    }

    @GetMapping("/redemptions")
    public List<RedemptionView> listRedemptions(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        List<Redemption> items = range == null
            ? redemptionRepository.findByChildId(principal.getUserId())
            : redemptionRepository.findByChildIdAndCreatedAtBetween(principal.getUserId(), range.start, range.end);
        return items.stream()
            .map(r -> new RedemptionView(
                r.getId(),
                r.getReward().getId(),
                r.getReward().getName(),
                r.getPointsCost(),
                r.getStatus(),
                r.getRedeemedAt(),
                r.getReviewNote(),
                r.getReviewedAt()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/points/export")
    public ResponseEntity<byte[]> exportPoints(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) throws Exception {
        ControllerDateRange range = ControllerDateRange.from(startDate, endDate);
        LocalDateTime start = range != null ? range.start : null;
        LocalDateTime end = range != null ? range.end : null;
        List<PointsLedger> ledgers;
        if (range != null) {
            ledgers = pointsLedgerRepository.findByChildIdAndCreatedAtBetween(principal.getUserId(), start, end);
        } else {
            ledgers = pointsLedgerRepository.findByChildId(principal.getUserId());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter writer = EasyExcel.write(out).build();
        try {
            List<SummaryRow> summaryData = buildPointsSummaryData(ledgers, start, end);
            List<ChildPointsRow> pointsData = ledgers.stream()
                .map(ledger -> new ChildPointsRow(
                    String.valueOf(ledger.getCreatedAt()),
                    String.valueOf(ledger.getType()),
                    ledger.getPoints(),
                    String.valueOf(ledger.getRefType()),
                    ledger.getNote() == null ? "" : ledger.getNote()
                ))
                .collect(Collectors.toList());

            WriteSheet summarySheet = EasyExcel.writerSheet("Summary")
                .head(SummaryRow.class)
                .build();
            writer.write(summaryData, summarySheet);

            WriteSheet pointsSheet = EasyExcel.writerSheet("Points")
                .head(ChildPointsRow.class)
                .build();
            writer.write(pointsData, pointsSheet);
        } finally {
            writer.finish();
        }

        byte[] bytes = out.toByteArray();
        String filename = "points-" + principal.getUserId() + "-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    private List<SummaryRow> buildPointsSummaryData(List<PointsLedger> ledgers, LocalDateTime start, LocalDateTime end) {
        int earnTotal = 0;
        int spendTotal = 0;
        int taskEarn = 0;
        int rewardSpend = 0;
        for (PointsLedger ledger : ledgers) {
            if (ledger.getType() == PointsType.EARN) {
                earnTotal += ledger.getPoints();
                if (ledger.getRefType() == PointsRefType.TASK) {
                    taskEarn += ledger.getPoints();
                }
            } else if (ledger.getType() == PointsType.SPEND) {
                spendTotal += ledger.getPoints();
                if (ledger.getRefType() == PointsRefType.REWARD) {
                    rewardSpend += ledger.getPoints();
                }
            }
        }

        List<SummaryRow> summaryData = new ArrayList<>();
        summaryData.add(new SummaryRow("Points Earned", String.valueOf(earnTotal)));
        summaryData.add(new SummaryRow("Points Spent", String.valueOf(spendTotal)));
        summaryData.add(new SummaryRow("Task Points", String.valueOf(taskEarn)));
        summaryData.add(new SummaryRow("Reward Spending", String.valueOf(rewardSpend)));
        summaryData.add(new SummaryRow("Current Balance", String.valueOf(earnTotal - spendTotal)));
        if (start != null) {
            summaryData.add(new SummaryRow("Start Date", String.valueOf(start.toLocalDate())));
        }
        if (end != null) {
            summaryData.add(new SummaryRow("End Date", String.valueOf(end.toLocalDate())));
        }
        return summaryData;
    }

    private TaskView toTaskView(Task task) {
        List<String> taskImages = ControllerImageSupport.parseImages(task.getTaskImages());
        List<String> checkinImages = ControllerImageSupport.parseImages(task.getCheckinImages());
        List<String> images = ControllerImageSupport.mergeImages(taskImages, checkinImages);
        return new TaskView(
            task.getId(),
            task.getChild().getId(),
            task.getTitle(),
            task.getDescription(),
            task.getPoints(),
            task.getStatus(),
            task.getSubmittedAt(),
            task.getCompletedAt(),
            task.getCheckinNote(),
            task.getCreatedAt(),
            images,
            taskImages,
            checkinImages
        );
    }

    private List<String> storeTaskImages(Long taskId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        Path root = Paths.get(uploadDir, "task-checkins", String.valueOf(taskId));
        Files.createDirectories(root);
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = root.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return Collections.singletonList("/uploads/task-checkins/" + taskId + "/" + filename);
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

    private Task findChildTask(Long childId, Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        if (!task.getChild().getId().equals(childId)) {
            return null;
        }
        return task;
    }

    private Optional<Long> findParentId(Long childId) {
        return parentChildRepository.findByChildId(childId).stream()
            .map(ParentChild::getParent)
            .map(User::getId)
            .findFirst();
    }

    private Reward findFamilyReward(Optional<Long> parentId, Long rewardId) {
        if (!parentId.isPresent()) {
            return null;
        }
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new IllegalStateException(RECORD_NOT_FOUND));
        if (!reward.getParent().getId().equals(parentId.get())) {
            return null;
        }
        return reward;
    }
}
