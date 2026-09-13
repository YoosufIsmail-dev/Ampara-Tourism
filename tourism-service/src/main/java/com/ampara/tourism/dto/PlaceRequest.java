package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class PlaceRequest {

    @NotBlank
    private String name;
    private String tamilName;
    private String sinhalaName;
    private String descriptionEn;
    private String descriptionTa;
    private String descriptionSi;
    private String category;
    private String tourismType;
    private Double latitude;
    private Double longitude;
    private String town;
    private String district;
    private String openingHours;
    private String entryFee;
    private String contactNumber;
    private String website;
    private String imageUrl;
    private Double rating;
    private List<String> activities;
    private Boolean parking;
    private Boolean wheelchairAccess;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTamilName() { return tamilName; }
    public void setTamilName(String tamilName) { this.tamilName = tamilName; }
    public String getSinhalaName() { return sinhalaName; }
    public void setSinhalaName(String sinhalaName) { this.sinhalaName = sinhalaName; }
    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }
    public String getDescriptionSi() { return descriptionSi; }
    public void setDescriptionSi(String descriptionSi) { this.descriptionSi = descriptionSi; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTourismType() { return tourismType; }
    public void setTourismType(String tourismType) { this.tourismType = tourismType; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getEntryFee() { return entryFee; }
    public void setEntryFee(String entryFee) { this.entryFee = entryFee; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public List<String> getActivities() { return activities; }
    public void setActivities(List<String> activities) { this.activities = activities; }
    public Boolean getParking() { return parking; }
    public void setParking(Boolean parking) { this.parking = parking; }
    public Boolean getWheelchairAccess() { return wheelchairAccess; }
    public void setWheelchairAccess(Boolean wheelchairAccess) { this.wheelchairAccess = wheelchairAccess; }
}
