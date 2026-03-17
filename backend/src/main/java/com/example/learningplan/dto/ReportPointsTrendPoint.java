package com.example.learningplan.dto;

public class ReportPointsTrendPoint {
    private String date;
    private int pointsEarned;
    private int pointsSpent;

    public ReportPointsTrendPoint(String date, int pointsEarned, int pointsSpent) {
        this.date = date;
        this.pointsEarned = pointsEarned;
        this.pointsSpent = pointsSpent;
    }

    public String getDate() {
        return date;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }
}
