package com.example.learningplan.dto;

import com.example.learningplan.entity.RedemptionStatus;

import java.time.LocalDateTime;

public class RedemptionParentView {
    private Long id;
    private Long rewardId;
    private String rewardName;
    private Long childId;
    private String childName;
    private Integer pointsCost;
    private RedemptionStatus status;
    private LocalDateTime redeemedAt;
    private String reviewNote;
    private LocalDateTime reviewedAt;

    public RedemptionParentView(Long id, Long rewardId, String rewardName, Long childId, String childName,
                                Integer pointsCost, RedemptionStatus status, LocalDateTime redeemedAt,
                                String reviewNote, LocalDateTime reviewedAt) {
        this.id = id;
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.childId = childId;
        this.childName = childName;
        this.pointsCost = pointsCost;
        this.status = status;
        this.redeemedAt = redeemedAt;
        this.reviewNote = reviewNote;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getRewardId() {
        return rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public RedemptionStatus getStatus() {
        return status;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
