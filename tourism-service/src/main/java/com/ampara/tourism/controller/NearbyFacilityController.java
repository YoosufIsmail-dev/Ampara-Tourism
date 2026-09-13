package com.ampara.tourism.controller;

import com.ampara.tourism.dto.NearbyFacilityRequest;
import com.ampara.tourism.entity.NearbyFacility;
import com.ampara.tourism.repository.NearbyFacilityRepository;
import com.ampara.tourism.service.GeoUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/nearby-facilities")
public class NearbyFacilityController {

    private final NearbyFacilityRepository repository;

    public NearbyFacilityController(NearbyFacilityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NearbyFacility> list(@RequestParam(required = false) String type,
                                      @RequestParam(required = false) String town) {
        if (type != null) {
            return repository.findByTypeIgnoreCase(type);
        }
        if (town != null) {
            return repository.findByTownIgnoreCase(town);
        }
        return repository.findAll();
    }

    /**
     * Nearest hospitals/ATMs/banks to a lat/lng, optionally filtered by type
     * (HOSPITAL, CLINIC, ATM, BANK), sorted by distance and limited (default 10).
     */
    @GetMapping("/nearby")
    public List<NearbyFacility> nearby(@RequestParam double lat, @RequestParam double lng,
                                        @RequestParam(required = false) String type,
                                        @RequestParam(defaultValue = "10") int limit) {
        return repository.findAll().stream()
                .filter(f -> type == null || f.getType().equalsIgnoreCase(type))
                .sorted(Comparator.comparingDouble(f -> GeoUtil.distanceKm(lat, lng, f.getLatitude(), f.getLongitude())))
                .limit(limit)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NearbyFacility> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NearbyFacility create(@Valid @RequestBody NearbyFacilityRequest request) {
        return repository.save(fromRequest(new NearbyFacility(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NearbyFacility> update(@PathVariable Long id, @Valid @RequestBody NearbyFacilityRequest request) {
        return repository.findById(id)
                .map(existing -> ResponseEntity.ok(repository.save(fromRequest(existing, request))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private NearbyFacility fromRequest(NearbyFacility f, NearbyFacilityRequest r) {
        f.setName(r.getName());
        f.setType(r.getType());
        f.setLatitude(r.getLatitude());
        f.setLongitude(r.getLongitude());
        f.setTown(r.getTown());
        f.setDistrict(r.getDistrict());
        f.setAddress(r.getAddress());
        f.setPhoneNumber(r.getPhoneNumber());
        f.setOperatorOrBank(r.getOperatorOrBank());
        if (r.getOpen24Hours() != null) f.setOpen24Hours(r.getOpen24Hours());
        return f;
    }
}
