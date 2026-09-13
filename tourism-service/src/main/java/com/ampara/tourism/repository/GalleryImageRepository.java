package com.ampara.tourism.repository;

import com.ampara.tourism.entity.GalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    List<GalleryImage> findByPlaceIdOrderBySortOrderAsc(Long placeId);

    long countByPlaceId(Long placeId);
}
