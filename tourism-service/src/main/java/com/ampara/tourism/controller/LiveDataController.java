package com.ampara.tourism.controller;

import com.ampara.tourism.live.GoogleMapsLiveService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/live")
public class LiveDataController {
    private final GoogleMapsLiveService googleMaps;

    public LiveDataController(GoogleMapsLiveService googleMaps) {
        this.googleMaps = googleMaps;
    }

    @GetMapping("/place")
    public Map<String, Object> place(@RequestParam String query,
                                     @RequestParam(required = false) Double lat,
                                     @RequestParam(required = false) Double lng) {
        return googleMaps.searchPlace(query, lat, lng);
    }

    @GetMapping("/route")
    public Map<String, Object> route(@RequestParam double originLat,
                                     @RequestParam double originLng,
                                     @RequestParam double destinationLat,
                                     @RequestParam double destinationLng,
                                     @RequestParam(defaultValue = "DRIVE") String mode,
                                     @RequestParam(required = false) String departureTime) {
        return googleMaps.computeRoute(originLat, originLng, destinationLat, destinationLng, mode, departureTime);
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "googleMapsConfigured", googleMaps.enabled(),
                "cacheMinutes", 5,
                "notes", "Live place hours/status and route/transit results depend on Google Maps Platform coverage and API configuration."
        );
    }
}
