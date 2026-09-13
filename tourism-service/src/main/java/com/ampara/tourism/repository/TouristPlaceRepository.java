package com.ampara.tourism.repository;

import com.ampara.tourism.entity.TouristPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TouristPlaceRepository extends JpaRepository<TouristPlace, Long> {

    List<TouristPlace> findByTownIgnoreCase(String town);

    List<TouristPlace> findByCategoryIgnoreCase(String category);

    List<TouristPlace> findByTownIgnoreCaseAndCategoryIgnoreCase(String town, String category);

    List<TouristPlace> findByNameContainingIgnoreCaseOrTamilNameContainingIgnoreCase(String name, String tamilName);
}
