package com.ampara.tourism.repository;

import com.ampara.tourism.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByDistrictIgnoreCase(String district);

    List<Attraction> findByCategoryIgnoreCase(String category);

    List<Attraction> findByDistrictIgnoreCaseAndCategoryIgnoreCase(String district, String category);
}
