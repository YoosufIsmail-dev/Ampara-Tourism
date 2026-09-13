package com.ampara.tourism.repository;

import com.ampara.tourism.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPlaceId(Long placeId);
    List<Review> findByHotelId(Long hotelId);
}
