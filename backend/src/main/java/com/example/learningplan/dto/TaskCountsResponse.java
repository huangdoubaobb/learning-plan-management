package com.example.learningplan.dto;

public class TaskCountsResponse {
    private long pending;
    private long completed;
    private long overdue;

    public TaskCountsResponse(long pending, long completed, long overdue) {
        this.pending = pending;
        this.completed = completed;
        this.overdue = overdue;
    }

    public long getPending() {
        return pending;
    }

    public long getCompleted() {
        return completed;
    }

    public long getOverdue() {
        return overdue;
    }
}
