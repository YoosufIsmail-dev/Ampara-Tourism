package com.ampara.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hotel", indexes = { @Index(name = "idx_hotel_coords", columnList = "latitude,longitude"), @Index(name = "idx_hotel_town", columnList = "town") })
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String district;
    private String town;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double pricePerNight;
    private Double rating;
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    public Hotel() {
    }

    public Hotel(String name, String district, String address, Double pricePerNight,
                 Double rating, String imageUrl, String description) {
        this.name = name;
        this.district = district;
        this.address = address;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
