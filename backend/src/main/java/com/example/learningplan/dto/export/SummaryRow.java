package com.example.learningplan.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;

public class SummaryRow {
    @ExcelProperty("Label")
    private String label;

    @ExcelProperty("Value")
    private String value;

    public SummaryRow() {}

    public SummaryRow(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
