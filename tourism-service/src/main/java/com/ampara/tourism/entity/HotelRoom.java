package com.ampara.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hotel_room", indexes = { @Index(name = "idx_room_hotel", columnList = "hotel_id") })
public class HotelRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(nullable = false)
    private String roomType;
    private Double pricePerNight;
    private Integer capacity;
    private Boolean available = true;
    private String amenities;
    private String imageUrl;

    public HotelRoom() {}

    public HotelRoom(Hotel hotel, String roomType, Double pricePerNight, Integer capacity,
                      Boolean available, String amenities, String imageUrl) {
        this.hotel = hotel; this.roomType = roomType; this.pricePerNight = pricePerNight;
        this.capacity = capacity; this.available = available; this.amenities = amenities; this.imageUrl = imageUrl;
    }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Hotel getHotel(){return hotel;} public void setHotel(Hotel hotel){this.hotel=hotel;}
    public String getRoomType(){return roomType;} public void setRoomType(String v){roomType=v;}
    public Double getPricePerNight(){return pricePerNight;} public void setPricePerNight(Double v){pricePerNight=v;}
    public Integer getCapacity(){return capacity;} public void setCapacity(Integer v){capacity=v;}
    public Boolean getAvailable(){return available;} public void setAvailable(Boolean v){available=v;}
    public String getAmenities(){return amenities;} public void setAmenities(String v){amenities=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
}