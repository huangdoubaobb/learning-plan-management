package com.example.learningplan.dto;

import com.example.learningplan.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public class TaskView {
    private Long id;
    private Long childId;
    private String title;
    private String description;
    private Integer points;
    private TaskStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private String checkinNote;
    private LocalDateTime createdAt;
    private List<String> images;
    private List<String> taskImages;
    private List<String> checkinImages;

    public TaskView(Long id, Long childId, String title, String description, Integer points, TaskStatus status,
                    LocalDateTime submittedAt, LocalDateTime completedAt, String checkinNote,
                    LocalDateTime createdAt, List<String> images, List<String> taskImages, List<String> checkinImages) {
        this.id = id;
        this.childId = childId;
        this.title = title;
        this.description = description;
        this.points = points;
        this.status = status;
        this.submittedAt = submittedAt;
        this.completedAt = completedAt;
        this.checkinNote = checkinNote;
        this.createdAt = createdAt;
        this.images = images;
        this.taskImages = taskImages;
        this.checkinImages = checkinImages;
    }

    public Long getId() {
        return id;
    }

    public Long getChildId() {
        return childId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPoints() {
        return points;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getCheckinNote() {
        return checkinNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getTaskImages() {
        return taskImages;
    }

    public List<String> getCheckinImages() {
        return checkinImages;
    }
}
