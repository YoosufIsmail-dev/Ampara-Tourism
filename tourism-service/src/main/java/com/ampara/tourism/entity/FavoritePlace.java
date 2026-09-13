package com.ampara.tourism.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "favorite_place", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "place_id"}))
public class FavoritePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "authorities"})
    private User user;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    public FavoritePlace() {
    }

    public FavoritePlace(User user, Long placeId) {
        this.user = user;
        this.placeId = placeId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
}
