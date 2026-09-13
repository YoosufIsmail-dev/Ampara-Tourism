package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;

public class EmergencyContactRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotBlank
    private String phoneNumber;

    private String district;
    private String town;
    private String address;
    private String notes;
    private Boolean nationwide;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Boolean getNationwide() { return nationwide; }
    public void setNationwide(Boolean nationwide) { this.nationwide = nationwide; }
}
