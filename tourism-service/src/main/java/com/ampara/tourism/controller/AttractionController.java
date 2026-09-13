package com.ampara.tourism.controller;

import com.ampara.tourism.dto.AttractionRequest;
import com.ampara.tourism.entity.Attraction;
import com.ampara.tourism.repository.AttractionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    private final AttractionRepository attractionRepository;

    public AttractionController(AttractionRepository attractionRepository) {
        this.attractionRepository = attractionRepository;
    }

    @GetMapping
    public List<Attraction> list(@RequestParam(required = false) String district,
                                  @RequestParam(required = false) String category) {
        if (district != null && category != null) {
            return attractionRepository.findByDistrictIgnoreCaseAndCategoryIgnoreCase(district, category);
        }
        if (district != null) {
            return attractionRepository.findByDistrictIgnoreCase(district);
        }
        if (category != null) {
            return attractionRepository.findByCategoryIgnoreCase(category);
        }
        return attractionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attraction> get(@PathVariable Long id) {
        return attractionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Attraction create(@Valid @RequestBody AttractionRequest request) {
        Attraction attraction = new Attraction(
                request.getName(),
                request.getDescription(),
                request.getDistrict(),
                request.getCategory(),
                request.getLatitude(),
                request.getLongitude(),
                request.getBestSeason(),
                request.getSuggestedDurationDays(),
                request.getActivities()
        );
        return attractionRepository.save(attraction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Attraction> update(@PathVariable Long id, @Valid @RequestBody AttractionRequest request) {
        return attractionRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setDescription(request.getDescription());
                    existing.setDistrict(request.getDistrict());
                    existing.setCategory(request.getCategory());
                    existing.setLatitude(request.getLatitude());
                    existing.setLongitude(request.getLongitude());
                    existing.setBestSeason(request.getBestSeason());
                    existing.setSuggestedDurationDays(request.getSuggestedDurationDays());
                    existing.setActivities(request.getActivities());
                    return ResponseEntity.ok(attractionRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!attractionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        attractionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
