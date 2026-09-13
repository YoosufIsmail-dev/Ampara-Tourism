package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EventRequest {

    @NotBlank
    private String title;

    private String titleTa;
    private String titleSi;
    private String description;
    private String category;

    @NotNull
    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;
    private String location;
    private String town;
    private Long placeId;
    private Boolean recurringYearly;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }
    public String getTitleSi() { return titleSi; }
    public void setTitleSi(String titleSi) { this.titleSi = titleSi; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public Boolean getRecurringYearly() { return recurringYearly; }
    public void setRecurringYearly(Boolean recurringYearly) { this.recurringYearly = recurringYearly; }
}
