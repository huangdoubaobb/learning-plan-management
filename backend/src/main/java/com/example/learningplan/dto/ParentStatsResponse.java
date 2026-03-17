package com.example.learningplan.dto;

public class ParentStatsResponse {
    private long childrenTotal;
    private long tasksTotal;
    private long rewardsTotal;
    private long pendingRedemptions;
    private long tasksInRange;
    private long redemptionsInRange;

    public ParentStatsResponse(long childrenTotal, long tasksTotal, long rewardsTotal, long pendingRedemptions,
                               long tasksInRange, long redemptionsInRange) {
        this.childrenTotal = childrenTotal;
        this.tasksTotal = tasksTotal;
        this.rewardsTotal = rewardsTotal;
        this.pendingRedemptions = pendingRedemptions;
        this.tasksInRange = tasksInRange;
        this.redemptionsInRange = redemptionsInRange;
    }

    public long getChildrenTotal() {
        return childrenTotal;
    }

    public long getTasksTotal() {
        return tasksTotal;
    }

    public long getRewardsTotal() {
        return rewardsTotal;
    }

    public long getPendingRedemptions() {
        return pendingRedemptions;
    }

    public long getTasksInRange() {
        return tasksInRange;
    }

    public long getRedemptionsInRange() {
        return redemptionsInRange;
    }
}
