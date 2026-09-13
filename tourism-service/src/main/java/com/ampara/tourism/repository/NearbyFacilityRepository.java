package com.ampara.tourism.repository;

import com.ampara.tourism.entity.NearbyFacility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NearbyFacilityRepository extends JpaRepository<NearbyFacility, Long> {

    List<NearbyFacility> findByTypeIgnoreCase(String type);

    List<NearbyFacility> findByTownIgnoreCase(String town);
}
