package com.ampara.tourism.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tourist_place")
public class TouristPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String tamilName;

    private String sinhalaName;

    @Column(length = 1000)
    private String descriptionEn;

    @Column(length = 1000)
    private String descriptionTa;

    @Column(length = 1000)
    private String descriptionSi;

    private String category; // Beach, Temple, Mosque, Church, Museum, Nature, Wildlife, Lake, Lagoon, Waterfall, Camping, Surfing, Historical, Harbour

    /** High-level tourism classification used by the town dashboard. */
    private String tourismType; // CULTURAL, NATURAL, OTHER

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

    @ElementCollection
    @CollectionTable(name = "place_activity", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "activity")
    private List<String> activities = new ArrayList<>();

    private Boolean parking = false;
    private Boolean wheelchairAccess = false;

    public TouristPlace() {
    }

    public String getMapsUrl() {
        if (latitude == null || longitude == null) return null;
        return "https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

    /** Returns the name in the requested language ("ta"/"si"/"en"), falling back to English if missing. */
    public String getLocalizedName(String lang) {
        if (lang == null) return name;
        return switch (lang.toLowerCase()) {
            case "ta" -> (tamilName != null && !tamilName.isBlank()) ? tamilName : name;
            case "si" -> (sinhalaName != null && !sinhalaName.isBlank()) ? sinhalaName : name;
            default -> name;
        };
    }

    /** Returns the description in the requested language ("ta"/"si"/"en"), falling back to English if missing. */
    public String getLocalizedDescription(String lang) {
        if (lang == null) return descriptionEn;
        return switch (lang.toLowerCase()) {
            case "ta" -> (descriptionTa != null && !descriptionTa.isBlank()) ? descriptionTa : descriptionEn;
            case "si" -> (descriptionSi != null && !descriptionSi.isBlank()) ? descriptionSi : descriptionEn;
            default -> descriptionEn;
        };
    }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    /**
     * Returns the high-level tourism type. Existing records without a stored
     * value are classified automatically from their category.
     */
    public String getTourismType() {
        if (tourismType != null && !tourismType.isBlank()) return tourismType;
        if (category == null) return "OTHER";
        String c = category.trim().toLowerCase();
        return switch (c) {
            case "temple", "mosque", "church", "museum", "historical", "heritage",
                 "archaeological", "cultural", "market", "monument", "religious",
                 "shrine", "kovil" -> "CULTURAL";
            case "beach", "nature", "wildlife", "lake", "lagoon", "waterfall",
                 "forest", "mountain", "cave", "river", "reservoir", "park",
                 "birdwatching", "wetland", "viewpoint", "camping", "surfing" -> "NATURAL";
            default -> "OTHER";
        };
    }

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
