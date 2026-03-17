package com.example.learningplan.dto;

public class ReportSummaryResponse {
    private Long childId;
    private long tasksTotal;
    private long tasksCompleted;
    private int pointsBalance;
    private int pointsEarned;
    private int pointsSpent;
    private long rewardsRedeemed;

    public ReportSummaryResponse(Long childId, long tasksTotal, long tasksCompleted, int pointsBalance,
                                 int pointsEarned, int pointsSpent, long rewardsRedeemed) {
        this.childId = childId;
        this.tasksTotal = tasksTotal;
        this.tasksCompleted = tasksCompleted;
        this.pointsBalance = pointsBalance;
        this.pointsEarned = pointsEarned;
        this.pointsSpent = pointsSpent;
        this.rewardsRedeemed = rewardsRedeemed;
    }

    public Long getChildId() {
        return childId;
    }

    public long getTasksTotal() {
        return tasksTotal;
    }

    public long getTasksCompleted() {
        return tasksCompleted;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }

    public long getRewardsRedeemed() {
        return rewardsRedeemed;
    }
}
