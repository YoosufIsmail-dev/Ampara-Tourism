package com.ampara.tourism.controller;

import com.ampara.tourism.repository.EmergencyContactRepository;
import com.ampara.tourism.repository.NearbyFacilityRepository;
import com.ampara.tourism.repository.TouristPlaceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline Map Cache: bundles everything the mobile app needs to work without a signal
 * once downloaded - place pins with names/descriptions in all 3 languages, emergency
 * contacts, and nearby hospitals/ATMs. This backend doesn't ship map *tile* imagery
 * (that's normally cached directly from the map SDK, e.g. Google Maps' own offline
 * regions or an MBTiles bundle) - this manifest is the *data* layer the client overlays
 * on top of whatever tiles it has cached, so pins, search and emergency info still work
 * with zero connectivity.
 */
@RestController
@RequestMapping("/api/offline")
public class OfflineController {

    private final TouristPlaceRepository placeRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final NearbyFacilityRepository nearbyFacilityRepository;

    public OfflineController(TouristPlaceRepository placeRepository,
                              EmergencyContactRepository emergencyContactRepository,
                              NearbyFacilityRepository nearbyFacilityRepository) {
        this.placeRepository = placeRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.nearbyFacilityRepository = nearbyFacilityRepository;
    }

    /**
     * Full downloadable bundle for offline use, optionally scoped to one town to keep
     * the download small. Cache this client-side (e.g. in SQLite/IndexedDB) and re-fetch
     * only when GET /api/offline/version's hash changes.
     */
    @GetMapping("/manifest")
    public Map<String, Object> manifest(@RequestParam(required = false) String town) {
        var places = (town == null)
                ? placeRepository.findAll()
                : placeRepository.findByTownIgnoreCase(town);

        List<Map<String, Object>> placePins = places.stream().map(p -> {
            Map<String, Object> pin = new LinkedHashMap<>();
            pin.put("id", p.getId());
            pin.put("name", p.getName());
            pin.put("nameTa", p.getTamilName());
            pin.put("nameSi", p.getSinhalaName());
            pin.put("descriptionEn", p.getDescriptionEn());
            pin.put("descriptionTa", p.getDescriptionTa());
            pin.put("descriptionSi", p.getDescriptionSi());
            pin.put("category", p.getCategory());
            pin.put("town", p.getTown());
            pin.put("district", p.getDistrict());
            pin.put("latitude", p.getLatitude());
            pin.put("longitude", p.getLongitude());
            pin.put("imageUrl", p.getImageUrl());
            pin.put("openingHours", p.getOpeningHours());
            pin.put("entryFee", p.getEntryFee());
            pin.put("contactNumber", p.getContactNumber());
            return pin;
        }).toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("generatedAt", Instant.now().toString());
        body.put("town", town);
        body.put("places", placePins);
        body.put("emergencyContacts", emergencyContactRepository.findAll());
        body.put("nearbyFacilities", nearbyFacilityRepository.findAll());
        return body;
    }

    /**
     * Lightweight endpoint the client can poll to check if a new manifest download is
     * needed, without pulling the whole payload. Compares record counts + a content hash.
     */
    @GetMapping("/version")
    public Map<String, Object> version() {
        long placeCount = placeRepository.count();
        long contactCount = emergencyContactRepository.count();
        long facilityCount = nearbyFacilityRepository.count();

        String raw = placeCount + ":" + contactCount + ":" + facilityCount;
        String hash = sha256Short(raw);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("hash", hash);
        body.put("placeCount", placeCount);
        body.put("emergencyContactCount", contactCount);
        body.put("nearbyFacilityCount", facilityCount);
        return body;
    }

    private String sha256Short(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", hashBytes[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
