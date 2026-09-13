package com.ampara.tourism.repository;

import com.ampara.tourism.entity.FavoritePlace;
import com.ampara.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {
    List<FavoritePlace> findByUser(User user);
    Optional<FavoritePlace> findByUserAndPlaceId(User user, Long placeId);
    void deleteByUserAndPlaceId(User user, Long placeId);
}
