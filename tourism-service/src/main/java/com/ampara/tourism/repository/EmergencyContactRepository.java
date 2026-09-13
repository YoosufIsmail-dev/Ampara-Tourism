package com.ampara.tourism.repository;

import com.ampara.tourism.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByCategoryIgnoreCase(String category);

    List<EmergencyContact> findByDistrictIgnoreCaseOrNationwideTrue(String district);

    List<EmergencyContact> findByNationwideTrue();
}
