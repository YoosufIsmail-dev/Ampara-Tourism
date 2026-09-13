package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NearbyFacilityRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String town;
    private String district;
    private String address;
    private String phoneNumber;
    private String operatorOrBank;
    private Boolean open24Hours;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getOperatorOrBank() { return operatorOrBank; }
    public void setOperatorOrBank(String operatorOrBank) { this.operatorOrBank = operatorOrBank; }
    public Boolean getOpen24Hours() { return open24Hours; }
    public void setOpen24Hours(Boolean open24Hours) { this.open24Hours = open24Hours; }
}
