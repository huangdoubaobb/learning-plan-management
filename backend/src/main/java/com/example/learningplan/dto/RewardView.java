package com.example.learningplan.dto;

import com.example.learningplan.entity.RewardStatus;

public class RewardView {
    private Long id;
    private String name;
    private String description;
    private Integer pointsCost;
    private Integer stock;
    private RewardStatus status;
    private java.util.List<String> images;

    public RewardView(Long id, String name, String description, Integer pointsCost, Integer stock, RewardStatus status,
                      java.util.List<String> images) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsCost = pointsCost;
        this.stock = stock;
        this.status = status;
        this.images = images;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public Integer getStock() {
        return stock;
    }

    public RewardStatus getStatus() {
        return status;
    }

    public java.util.List<String> getImages() {
        return images;
    }
}
