package com.example.learningplan.dto;

public class TaskCompleteRequest {
    private String checkinAt;
    private String note;

    public String getCheckinAt() {
        return checkinAt;
    }

    public void setCheckinAt(String checkinAt) {
        this.checkinAt = checkinAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
