package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class ChildPointsRow {
    @ExcelProperty("时间")
    private String time;

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("积分")
    private Integer points;

    @ExcelProperty("关联类型")
    private String refType;

    @ExcelProperty("备注")
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
