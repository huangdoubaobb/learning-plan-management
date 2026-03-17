package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class TaskImportRow {
    @ExcelProperty("孩子ID")
    private Long childId;

    @ExcelProperty("孩子名称")
    private String childName;

    @ExcelProperty("任务标题")
    private String title;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("积分")
    private Integer points;

    public TaskImportRow() {}

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
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
}