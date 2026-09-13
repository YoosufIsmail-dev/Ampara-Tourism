package com.ampara.tourism.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "place_view")
public class PlaceView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long placeId;

    private String placeName;
    private String category;
    private String town;

    /** en, ta, si - which language the visitor viewed the place in */
    private String language;

    /** e.g. "app", "web", "qr" - where the view originated */
    private String source;

    @Column(nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    public PlaceView() {
    }

    public PlaceView(Long placeId, String placeName, String category, String town, String language, String source) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.category = category;
        this.town = town;
        this.language = language;
        this.source = source;
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
