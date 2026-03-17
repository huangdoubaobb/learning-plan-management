package com.example.learningplan.dto;

public class ReportTrendPoint {
    private String date;
    private long tasksTotal;
    private long tasksCompleted;
    private double completionRate;

    public ReportTrendPoint(String date, long tasksTotal, long tasksCompleted, double completionRate) {
        this.date = date;
        this.tasksTotal = tasksTotal;
        this.tasksCompleted = tasksCompleted;
        this.completionRate = completionRate;
    }

    public String getDate() {
        return date;
    }

    public long getTasksTotal() {
        return tasksTotal;
    }

    public long getTasksCompleted() {
        return tasksCompleted;
    }

    public double getCompletionRate() {
        return completionRate;
    }
}
