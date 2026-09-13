package com.ampara.tourism.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tourism_event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String titleTa;
    private String titleSi;

    @Column(length = 1000)
    private String description;

    /** FESTIVAL, RELIGIOUS, CULTURAL, SPORTS, MARKET, OTHER */
    private String category;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String location;
    private String town;

    /** Optional link to a TouristPlace this event happens at. */
    private Long placeId;

    private Boolean recurringYearly = false;

    public Event() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
