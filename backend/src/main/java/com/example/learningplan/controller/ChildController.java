package com.example.learningplan.controller;

import com.example.learningplan.config.UserPrincipal;
import com.example.learningplan.dto.RedemptionView;
import com.example.learningplan.dto.RewardView;
import com.example.learningplan.dto.TaskCompleteRequest;
import com.example.learningplan.dto.TaskView;
import com.example.learningplan.dto.UserView;
import com.example.learningplan.entity.*;
import com.example.learningplan.repository.*;
import com.example.learningplan.service.PointsService;
import com.example.learningplan.dto.export.ChildPointsRow;
import com.example.learningplan.dto.export.SummaryRow;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/child")
@PreAuthorize("hasRole('CHILD')")
public class ChildController {
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
            .orElseThrow(() -> new IllegalStateException("Not found"));
        return new UserView(child.getId(), child.getUsername(), child.getDisplayName(),
            primaryRoleCode(child.getId()), child.getPoints(), child.getEnabled());
    }

    @GetMapping("/tasks")
    public List<TaskView> listTasks(@AuthenticationPrincipal UserPrincipal principal,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        List<Task> tasks = range == null
            ? taskRepository.findByChildId(principal.getUserId())
            : taskRepository.findByChildIdAndCreatedAtBetween(principal.getUserId(), range.start, range.end);
        return tasks.stream()
            .map(task -> {
                List<String> taskImages = parseImages(task.getTaskImages());
                List<String> checkinImages = parseImages(task.getCheckinImages());
                List<String> images = mergeImages(taskImages, checkinImages);
                return new TaskView(task.getId(), task.getChild().getId(), task.getTitle(), task.getDescription(), task.getPoints(), task.getStatus(),
                    task.getSubmittedAt(), task.getCompletedAt(), task.getCheckinNote(), task.getCreatedAt(),
                    images, taskImages, checkinImages);
            })
            .collect(Collectors.toList());
    }

    @PostMapping("/tasks/{id}/submit")
    @Transactional
    public ResponseEntity<?> submitTask(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @RequestParam(value = "note", required = false) String note,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getChild().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前孩子");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("任务已完成");
        }
        if (task.getSubmittedAt() != null) {
            return ResponseEntity.badRequest().body("任务已提交审核");
        }
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                return ResponseEntity.badRequest().body("只支持JPG/PNG图片");
            }
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                String ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();
                if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
                    return ResponseEntity.badRequest().body("只支持JPG/PNG图片");
                }
            }
            if (!isImageMagic(file)) {
                return ResponseEntity.badRequest().body("图片格式不正确");
            }
            if (file.getSize() > 10 * 1024 * 1024L) {
                return ResponseEntity.badRequest().body("图片大小不能超过10MB");
            }
        }
        List<String> imageUrls = storeTaskImages(task.getId(), file);
        if (note != null && !note.trim().isEmpty()) {
            task.setCheckinNote(note.trim());
        }
        task.setCheckinImages(String.join(",", imageUrls));
        task.setSubmittedAt(LocalDateTime.now());
        taskRepository.save(task);
        return ResponseEntity.ok("已提交审核");
    }

    @PostMapping("/tasks/{id}/complete")
    @Transactional
    public ResponseEntity<?> completeTask(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id,
                                          @RequestBody(required = false) TaskCompleteRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        if (!task.getChild().getId().equals(principal.getUserId())) {
            return ResponseEntity.badRequest().body("任务不属于当前孩子");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.ok("任务已完成");
        }
        if (task.getSubmittedAt() != null) {
            return ResponseEntity.ok("任务已提交审核");
        }
        if (request != null && request.getNote() != null && !request.getNote().trim().isEmpty()) {
            task.setCheckinNote(request.getNote().trim());
        }
        task.setSubmittedAt(LocalDateTime.now());
        taskRepository.save(task);

        return ResponseEntity.ok("已提交审核");
    }

    @GetMapping("/rewards")
    public List<RewardView> listRewards(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate) {
        Optional<Long> parentId = parentChildRepository.findByChildId(principal.getUserId()).stream()
            .map(pc -> pc.getParent().getId())
            .findFirst();
        if (!parentId.isPresent()) {
            return Collections.emptyList();
        }
        DateRange range = DateRange.from(startDate, endDate);
        List<Reward> rewards = range == null
            ? rewardRepository.findByParentIdAndStatus(parentId.get(), RewardStatus.ACTIVE)
            : rewardRepository.findByParentIdAndStatusAndCreatedAtBetween(parentId.get(), RewardStatus.ACTIVE, range.start, range.end);
        return rewards.stream()
            .map(reward -> new RewardView(reward.getId(), reward.getName(), reward.getDescription(), reward.getPointsCost(), reward.getStock(), reward.getStatus(),
                parseImages(reward.getImages())))
            .collect(Collectors.toList());
    }

    @PostMapping("/rewards/{id}/redeem")
    @Transactional
    public ResponseEntity<?> redeemReward(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
        Optional<Long> parentId = parentChildRepository.findByChildId(principal.getUserId()).stream()
            .map(pc -> pc.getParent().getId())
            .findFirst();
        if (!parentId.isPresent() || !reward.getParent().getId().equals(parentId.get())) {
            return ResponseEntity.badRequest().body("奖励不属于当前家庭");
        }
        if (reward.getStatus() != RewardStatus.ACTIVE) {
            return ResponseEntity.badRequest().body("奖励不可兑换");
        }
        if (reward.getStock() <= 0) {
            return ResponseEntity.badRequest().body("奖励库存不足");
        }
        User child = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Not found"));
        pointsService.spendPoints(child, reward.getPointsCost(), PointsRefType.REWARD, reward.getId(), "兑换奖励");

        reward.setStock(reward.getStock() - 1);
        rewardRepository.save(reward);

        Redemption redemption = new Redemption();
        redemption.setReward(reward);
        redemption.setChild(child);
        redemption.setPointsCost(reward.getPointsCost());
        redemption.setStatus(RedemptionStatus.PENDING);
        redemptionRepository.save(redemption);

        return ResponseEntity.ok("兑换成功，等待家长审核");
    }

    @GetMapping("/redemptions")
    public List<RedemptionView> listRedemptions(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        DateRange range = DateRange.from(startDate, endDate);
        List<Redemption> items = range == null
            ? redemptionRepository.findByChildId(principal.getUserId())
            : redemptionRepository.findByChildIdAndCreatedAtBetween(principal.getUserId(), range.start, range.end);
        return items.stream()
            .map(r -> new RedemptionView(r.getId(), r.getReward().getId(), r.getReward().getName(), r.getPointsCost(),
                r.getStatus(), r.getRedeemedAt(), r.getReviewNote(), r.getReviewedAt()))
            .collect(Collectors.toList());
    }

    @GetMapping("/points/export")
    public ResponseEntity<byte[]> exportPoints(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) throws Exception {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            start = LocalDate.parse(startDate.trim()).atStartOfDay();
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            end = LocalDate.parse(endDate.trim()).atTime(LocalTime.MAX);
        }

        List<PointsLedger> ledgers;
        if (start != null || end != null) {
            if (start == null) {
                start = LocalDate.of(2000, 1, 1).atStartOfDay();
            }
            if (end == null) {
                end = LocalDateTime.now();
            }
            ledgers = pointsLedgerRepository.findByChildIdAndCreatedAtBetween(principal.getUserId(), start, end);
        } else {
            ledgers = pointsLedgerRepository.findByChildId(principal.getUserId());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter writer = EasyExcel.write(out).build();
        try {
            List<SummaryRow> summaryData = new java.util.ArrayList<>();
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
            summaryData.add(new SummaryRow("累计获得", String.valueOf(earnTotal)));
            summaryData.add(new SummaryRow("累计消耗", String.valueOf(spendTotal)));
            summaryData.add(new SummaryRow("任务获得", String.valueOf(taskEarn)));
            summaryData.add(new SummaryRow("奖励兑换消耗", String.valueOf(rewardSpend)));
            summaryData.add(new SummaryRow("净积分变化", String.valueOf(earnTotal - spendTotal)));
            if (start != null) {
                summaryData.add(new SummaryRow("开始日期", String.valueOf(start.toLocalDate())));
            }
            if (end != null) {
                summaryData.add(new SummaryRow("结束日期", String.valueOf(end.toLocalDate())));
            }

            List<ChildPointsRow> pointsData = new java.util.ArrayList<>();
            for (PointsLedger ledger : ledgers) {
                pointsData.add(new ChildPointsRow(
                    String.valueOf(ledger.getCreatedAt()),
                    String.valueOf(ledger.getType()),
                    ledger.getPoints(),
                    String.valueOf(ledger.getRefType()),
                    ledger.getNote() == null ? "" : ledger.getNote()
                ));
            }

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

    private List<String> storeTaskImages(Long taskId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        Path root = Paths.get(uploadDir, "task-checkins", String.valueOf(taskId));
        Files.createDirectories(root);
        List<String> urls = new java.util.ArrayList<>();
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Collections.emptyList();
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = root.resolve(filename);
        Files.copy(file.getInputStream(), target);
        String url = "/uploads/task-checkins/" + taskId + "/" + filename;
        urls.add(url);
        return urls;
    }

    private List<String> parseImages(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private List<String> mergeImages(List<String> taskImages, List<String> checkinImages) {
        List<String> merged = new java.util.ArrayList<>();
        if (taskImages != null) merged.addAll(taskImages);
        if (checkinImages != null) merged.addAll(checkinImages);
        return merged;
    }

    private boolean isImageMagic(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = in.read(header);
            if (read < 3) return false;
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }
            // PNG: 89 50 4E 47 0D 0A 1A 0A
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






