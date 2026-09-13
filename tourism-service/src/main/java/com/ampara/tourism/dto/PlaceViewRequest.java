package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotNull;

public class PlaceViewRequest {

    @NotNull
    private Long placeId;

    private String language;
    private String source;

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
