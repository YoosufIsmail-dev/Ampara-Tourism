package com.ampara.tourism.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${app.google-maps.api-key:}")
    private String mapsApiKey;

    /**
     * Google Maps JS API keys are meant to be used client-side and restricted by
     * HTTP referrer in the Google Cloud Console - it's normal (and necessary) for
     * a frontend to fetch this at runtime rather than hardcode it.
     */
    @GetMapping("/maps-key")
    public Map<String, String> mapsKey() {
        if (mapsApiKey == null || mapsApiKey.isBlank()) {
            return Map.of("status", "Google Maps API key not configured yet - set GOOGLE_MAPS_API_KEY");
        }
        return Map.of("apiKey", mapsApiKey);
    }
}
