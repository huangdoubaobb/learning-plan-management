package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class TaskExportRow {
    @ExcelProperty("孩子")
    private String childName;

    @ExcelProperty("任务")
    private String title;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("积分")
    private Integer points;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("完成时间")
    private String completedAt;

    @ExcelProperty("备注")
    private String note;

    public TaskExportRow() {}

    public TaskExportRow(String childName, String title, String description, Integer points, String status,
                         String completedAt, String note) {
        this.childName = childName;
        this.title = title;
        this.description = description;
        this.points = points;
        this.status = status;
        this.completedAt = completedAt;
        this.note = note;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}