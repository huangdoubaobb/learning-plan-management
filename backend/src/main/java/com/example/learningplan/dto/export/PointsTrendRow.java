package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class PointsTrendRow {
    @ExcelProperty("Date")
    private String date;

    @ExcelProperty("Points Earned")
    private Integer pointsEarned;

    @ExcelProperty("Points Spent")
    private Integer pointsSpent;

    public PointsTrendRow() {}

    public PointsTrendRow(String date, Integer pointsEarned, Integer pointsSpent) {
        this.date = date;
        this.pointsEarned = pointsEarned;
        this.pointsSpent = pointsSpent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Integer getPointsSpent() {
        return pointsSpent;
    }

    public void setPointsSpent(Integer pointsSpent) {
        this.pointsSpent = pointsSpent;
    }
}
