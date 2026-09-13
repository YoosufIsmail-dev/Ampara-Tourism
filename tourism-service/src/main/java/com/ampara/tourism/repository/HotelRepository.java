package com.ampara.tourism.repository;

import com.ampara.tourism.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByDistrictIgnoreCase(String district);
    List<Hotel> findByTownIgnoreCase(String town);
    List<Hotel> findByLatitudeBetweenAndLongitudeBetween(double minLat, double maxLat, double minLon, double maxLon);
    List<Hotel> findByTownIgnoreCaseAndLatitudeIsNotNullAndLongitudeIsNotNull(String town);
}
