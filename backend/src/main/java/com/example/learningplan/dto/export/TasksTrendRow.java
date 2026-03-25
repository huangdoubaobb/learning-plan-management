package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class TasksTrendRow {
    @ExcelProperty("Date")
    private String date;

    @ExcelProperty("Total Tasks")
    private Long tasksTotal;

    @ExcelProperty("Completed Tasks")
    private Long tasksCompleted;

    @ExcelProperty("Completion Rate")
    private String completionRate;

    public TasksTrendRow() {}

    public TasksTrendRow(String date, Long tasksTotal, Long tasksCompleted, String completionRate) {
        this.date = date;
        this.tasksTotal = tasksTotal;
        this.tasksCompleted = tasksCompleted;
        this.completionRate = completionRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getTasksTotal() {
        return tasksTotal;
    }

    public void setTasksTotal(Long tasksTotal) {
        this.tasksTotal = tasksTotal;
    }

    public Long getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(Long tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public String getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(String completionRate) {
        this.completionRate = completionRate;
    }
}
