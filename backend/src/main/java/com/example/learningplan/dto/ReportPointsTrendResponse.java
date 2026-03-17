package com.example.learningplan.dto;

import java.util.List;

public class ReportPointsTrendResponse {
    private Long childId;
    private int pointsBalance;
    private List<ReportPointsTrendPoint> points;

    public ReportPointsTrendResponse(Long childId, int pointsBalance, List<ReportPointsTrendPoint> points) {
        this.childId = childId;
        this.pointsBalance = pointsBalance;
        this.points = points;
    }

    public Long getChildId() {
        return childId;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public List<ReportPointsTrendPoint> getPoints() {
        return points;
    }
}
