package com.ampara.tourism.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attraction")
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String district;

    private String category; // e.g. Wildlife, Beach, Heritage, Adventure

    private Double latitude;

    private Double longitude;

    private String bestSeason;

    private Integer suggestedDurationDays;

    @ElementCollection
    @CollectionTable(name = "attraction_activity", joinColumns = @JoinColumn(name = "attraction_id"))
    @Column(name = "activity")
    private List<String> activities = new ArrayList<>();

    public Attraction() {
    }

    public Attraction(String name, String description, String district, String category,
                       Double latitude, Double longitude, String bestSeason,
                       Integer suggestedDurationDays, List<String> activities) {
        this.name = name;
        this.description = description;
        this.district = district;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bestSeason = bestSeason;
        this.suggestedDurationDays = suggestedDurationDays;
        this.activities = activities;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
