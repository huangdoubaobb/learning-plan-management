package com.example.learningplan.dto;

import java.util.List;

public class ReportTrendResponse {
    private Long childId;
    private double completionRate;
    private List<ReportTrendPoint> points;

    public ReportTrendResponse(Long childId, double completionRate, List<ReportTrendPoint> points) {
        this.childId = childId;
        this.completionRate = completionRate;
        this.points = points;
    }

    public Long getChildId() {
        return childId;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public List<ReportTrendPoint> getPoints() {
        return points;
    }
}
