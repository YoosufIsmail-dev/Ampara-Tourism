package com.ampara.tourism.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gallery_image")
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    private String caption;
    private String captionTa;
    private String captionSi;

    private Integer sortOrder = 0;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public GalleryImage() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getCaptionTa() { return captionTa; }
    public void setCaptionTa(String captionTa) { this.captionTa = captionTa; }
    public String getCaptionSi() { return captionSi; }
    public void setCaptionSi(String captionSi) { this.captionSi = captionSi; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
