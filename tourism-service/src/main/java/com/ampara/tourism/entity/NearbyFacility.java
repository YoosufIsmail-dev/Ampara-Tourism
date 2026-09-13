package com.ampara.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nearby_facility")
public class NearbyFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** HOSPITAL, CLINIC, ATM, BANK */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String town;
    private String district;
    private String address;
    private String phoneNumber;

    /** For ATMs/banks: e.g. "Commercial Bank", "Sampath Bank"; for hospitals: "Government"/"Private" */
    private String operatorOrBank;

    private Boolean open24Hours = false;

    public NearbyFacility() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
