package com.ampara.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "food_item", indexes = { @Index(name = "idx_food_coords", columnList = "latitude,longitude"), @Index(name = "idx_food_town", columnList = "town") })
public class FoodItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String name;
    private String town;
    private String category;
    private String restaurantName;
    private Double price;
    @Column(length=1000) private String description;
    private String imageUrl;
    private Boolean vegetarian;
    private String contactNumber;
    private Double latitude;
    private Double longitude;

    public FoodItem() {}
    public FoodItem(String name, String town, String category, String restaurantName, Double price,
                    String description, String imageUrl, Boolean vegetarian, String contactNumber) {
        this.name=name; this.town=town; this.category=category; this.restaurantName=restaurantName;
        this.price=price; this.description=description; this.imageUrl=imageUrl;
        this.vegetarian=vegetarian; this.contactNumber=contactNumber;
    }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getTown(){return town;} public void setTown(String v){town=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getRestaurantName(){return restaurantName;} public void setRestaurantName(String v){restaurantName=v;}
    public Double getPrice(){return price;} public void setPrice(Double v){price=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public Boolean getVegetarian(){return vegetarian;} public void setVegetarian(Boolean v){vegetarian=v;}
    public String getContactNumber(){return contactNumber;} public void setContactNumber(String v){contactNumber=v;}
    public Double getLatitude(){return latitude;} public void setLatitude(Double v){latitude=v;}
    public Double getLongitude(){return longitude;} public void setLongitude(Double v){longitude=v;}
}