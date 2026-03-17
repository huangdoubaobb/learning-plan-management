package com.example.learningplan.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class RewardCreateRequest {
    @NotBlank
    private String name;
    private String description;

    @Min(0)
    private Integer pointsCost = 0;

    @Min(0)
    private Integer stock = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}

