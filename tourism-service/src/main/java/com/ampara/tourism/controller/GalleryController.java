package com.ampara.tourism.controller;

import com.ampara.tourism.dto.GalleryImageRequest;
import com.ampara.tourism.entity.GalleryImage;
import com.ampara.tourism.repository.GalleryImageRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Multi-image gallery per tourist place (beyond the single TouristPlace.imageUrl).
 * Upload the file first via /api/images (Cloudinary), then register the returned URL here.
 */
@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final GalleryImageRepository repository;

    public GalleryController(GalleryImageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/place/{placeId}")
    public List<GalleryImage> byPlace(@PathVariable Long placeId) {
        return repository.findByPlaceIdOrderBySortOrderAsc(placeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryImage create(@Valid @RequestBody GalleryImageRequest request) {
        GalleryImage image = new GalleryImage();
        image.setPlaceId(request.getPlaceId());
        image.setImageUrl(request.getImageUrl());
        image.setCaption(request.getCaption());
        image.setCaptionTa(request.getCaptionTa());
        image.setCaptionSi(request.getCaptionSi());
        image.setSortOrder(request.getSortOrder() != null ? request.getSortOrder()
                : (int) repository.countByPlaceId(request.getPlaceId()));
        return repository.save(image);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
