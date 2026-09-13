package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GalleryImageRequest {

    @NotNull
    private Long placeId;

    @NotBlank
    private String imageUrl;

    private String caption;
    private String captionTa;
    private String captionSi;
    private Integer sortOrder;

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
}
