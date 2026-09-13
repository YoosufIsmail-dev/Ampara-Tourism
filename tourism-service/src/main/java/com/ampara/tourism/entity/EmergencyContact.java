package com.ampara.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "emergency_contact")
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** POLICE, TOURIST_POLICE, HOSPITAL, AMBULANCE, FIRE, EMBASSY, DISASTER, OTHER */
    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String phoneNumber;

    private String district;
    private String town;
    private String address;
    private String notes;

    /** true for nationwide hotlines (e.g. 119 Police, 1990 Suwa Seriya) that apply regardless of district. */
    private Boolean nationwide = false;

    public EmergencyContact() {
    }

    public EmergencyContact(String name, String category, String phoneNumber, String district,
                             String town, String address, String notes, Boolean nationwide) {
        this.name = name;
        this.category = category;
        this.phoneNumber = phoneNumber;
        this.district = district;
        this.town = town;
        this.address = address;
        this.notes = notes;
        this.nationwide = nationwide;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
