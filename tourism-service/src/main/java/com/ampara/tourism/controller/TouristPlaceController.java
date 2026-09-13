package com.ampara.tourism.controller;

import com.ampara.tourism.dto.PlaceRequest;
import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.ampara.tourism.service.GeoUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/places")
public class TouristPlaceController {

    private final TouristPlaceRepository placeRepository;

    public TouristPlaceController(TouristPlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @GetMapping
    public List<TouristPlace> list(@RequestParam(required = false) String town,
                                    @RequestParam(required = false) String category) {
        if (town != null && category != null) {
            return placeRepository.findByTownIgnoreCaseAndCategoryIgnoreCase(town, category);
        }
        if (town != null) {
            return placeRepository.findByTownIgnoreCase(town);
        }
        if (category != null) {
            return placeRepository.findByCategoryIgnoreCase(category);
        }
        return placeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TouristPlace> get(@PathVariable Long id) {
        return placeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/town/{town}")
    public List<TouristPlace> byTown(@PathVariable String town) {
        return placeRepository.findByTownIgnoreCase(town);
    }

    @GetMapping("/category/{category}")
    public List<TouristPlace> byCategory(@PathVariable String category) {
        return placeRepository.findByCategoryIgnoreCase(category);
    }

    /**
     * Returns a place with its name/description resolved to the requested language.
     * lang: "en" (default), "ta" (தமிழ்), or "si" (සිංහල). Falls back to English if a
     * translation is missing for that field.
     */
    @GetMapping("/{id}/localized")
    public ResponseEntity<Map<String, Object>> localized(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "en") String lang) {
        return placeRepository.findById(id)
                .map(p -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("id", p.getId());
                    body.put("lang", lang);
                    body.put("name", p.getLocalizedName(lang));
                    body.put("description", p.getLocalizedDescription(lang));
                    body.put("category", p.getCategory());
                    body.put("tourismType", p.getTourismType());
                    body.put("town", p.getTown());
                    body.put("district", p.getDistrict());
                    body.put("latitude", p.getLatitude());
                    body.put("longitude", p.getLongitude());
                    body.put("openingHours", p.getOpeningHours());
                    body.put("entryFee", p.getEntryFee());
                    body.put("contactNumber", p.getContactNumber());
                    body.put("website", p.getWebsite());
                    body.put("imageUrl", p.getImageUrl());
                    body.put("rating", p.getRating());
                    body.put("activities", p.getActivities());
                    body.put("mapsUrl", p.getMapsUrl());
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<TouristPlace> search(@RequestParam String name) {
        return placeRepository.findByNameContainingIgnoreCaseOrTamilNameContainingIgnoreCase(name, name);
    }

    /** Nearest places to a given lat/lng, sorted by distance, limited by `limit` (default 10). */
    @GetMapping("/nearby")
    public List<TouristPlace> nearby(@RequestParam double lat, @RequestParam double lng,
                                      @RequestParam(defaultValue = "10") int limit) {
        return placeRepository.findAll().stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .sorted(Comparator.comparingDouble(p -> GeoUtil.distanceKm(lat, lng, p.getLatitude(), p.getLongitude())))
                .limit(limit)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TouristPlace create(@Valid @RequestBody PlaceRequest request) {
        return placeRepository.save(fromRequest(new TouristPlace(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TouristPlace> update(@PathVariable Long id, @Valid @RequestBody PlaceRequest request) {
        return placeRepository.findById(id)
                .map(existing -> ResponseEntity.ok(placeRepository.save(fromRequest(existing, request))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!placeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        placeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TouristPlace fromRequest(TouristPlace p, PlaceRequest r) {
        p.setName(r.getName());
        p.setTamilName(r.getTamilName());
        p.setSinhalaName(r.getSinhalaName());
        p.setDescriptionEn(r.getDescriptionEn());
        p.setDescriptionTa(r.getDescriptionTa());
        p.setDescriptionSi(r.getDescriptionSi());
        p.setCategory(r.getCategory());
        if (r.getTourismType() != null && !r.getTourismType().isBlank()) p.setTourismType(r.getTourismType().toUpperCase());
        p.setLatitude(r.getLatitude());
        p.setLongitude(r.getLongitude());
        p.setTown(r.getTown());
        p.setDistrict(r.getDistrict());
        p.setOpeningHours(r.getOpeningHours());
        p.setEntryFee(r.getEntryFee());
        p.setContactNumber(r.getContactNumber());
        p.setWebsite(r.getWebsite());
        p.setImageUrl(r.getImageUrl());
        p.setRating(r.getRating());
        p.setActivities(r.getActivities());
        if (r.getParking() != null) p.setParking(r.getParking());
        if (r.getWheelchairAccess() != null) p.setWheelchairAccess(r.getWheelchairAccess());
        return p;
    }
}
