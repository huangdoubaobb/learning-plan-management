package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class PointsTrendRow {
    @ExcelProperty("日期")
    private String date;

    @ExcelProperty("积分获取")
    private Integer pointsEarned;

    @ExcelProperty("积分消耗")
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
