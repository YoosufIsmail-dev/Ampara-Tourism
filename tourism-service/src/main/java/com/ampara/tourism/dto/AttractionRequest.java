package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class AttractionRequest {

    @NotBlank
    private String name;

    private String description;
    private String district;
    private String category;
    private Double latitude;
    private Double longitude;
    private String bestSeason;
    private Integer suggestedDurationDays;
    private List<String> activities;

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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getBestSeason() {
        return bestSeason;
    }

    public void setBestSeason(String bestSeason) {
        this.bestSeason = bestSeason;
    }

    public Integer getSuggestedDurationDays() {
        return suggestedDurationDays;
    }

    public void setSuggestedDurationDays(Integer suggestedDurationDays) {
        this.suggestedDurationDays = suggestedDurationDays;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }
}
