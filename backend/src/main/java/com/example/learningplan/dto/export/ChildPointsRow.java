package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class ChildPointsRow {
    @ExcelProperty("Time")
    private String time;

    @ExcelProperty("Type")
    private String type;

    @ExcelProperty("Points")
    private Integer points;

    @ExcelProperty("Reference Type")
    private String refType;

    @ExcelProperty("Note")
    private String note;

    public ChildPointsRow() {}

    public ChildPointsRow(String time, String type, Integer points, String refType, String note) {
        this.time = time;
        this.type = type;
        this.points = points;
        this.refType = refType;
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
