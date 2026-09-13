package com.ampara.tourism.live;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.time.Duration;

@Service
public class GoogleMapsLiveService {

    private final RestClient client;
    private final Cache<String, Map<String, Object>> cache;

    public GoogleMapsLiveService(
            @Value("${integration.http.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${integration.http.read-timeout-ms:10000}") int readTimeoutMs,
            @Value("${app.cache.external-max-entries:1000}") int maxEntries) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.max(1000, connectTimeoutMs));
        factory.setReadTimeout(Math.max(2000, readTimeoutMs));
        this.client = RestClient.builder().requestFactory(factory).build();
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(100, maxEntries))
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    @Value("${app.google-maps.api-key:}")
    private String apiKey;

    @Value("${app.google-maps.places-url:https://places.googleapis.com/v1/places:searchText}")
    private String placesUrl;

    @Value("${app.google-maps.routes-url:https://routes.googleapis.com/directions/v2:computeRoutes}")
    private String routesUrl;

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchPlace(String query, Double latitude, Double longitude) {
        if (!enabled()) return Map.of("status", "NOT_CONFIGURED", "query", query);
        String key = "place:" + query.toLowerCase(Locale.ROOT) + ":" + (latitude == null ? "" : String.format(Locale.ROOT, "%.4f", latitude)) + ":" + (longitude == null ? "" : String.format(Locale.ROOT, "%.4f", longitude));
        Map<String, Object> cached = cache.getIfPresent(key);
        if (cached != null) return cached;

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("textQuery", query);
            body.put("languageCode", "en");
            body.put("pageSize", 5);
            if (latitude != null && longitude != null) {
                body.put("locationBias", Map.of("circle", Map.of(
                        "center", Map.of("latitude", latitude, "longitude", longitude),
                        "radius", 8000.0)));
            }
            String fields = String.join(",",
                    "places.id", "places.displayName", "places.formattedAddress", "places.location",
                    "places.primaryType", "places.businessStatus", "places.currentOpeningHours",
                    "places.rating", "places.userRatingCount", "places.googleMapsUri", "places.websiteUri"
            );
            Map<String, Object> response = client.post().uri(placesUrl)
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", fields)
                    .body(body)
                    .retrieve().body(Map.class);
            if (response == null) response = Map.of();
            Map<String, Object> normalized = new LinkedHashMap<>();
            normalized.put("status", "LIVE");
            normalized.put("queriedAt", OffsetDateTime.now(ZoneOffset.UTC).toString());
            normalized.put("query", query);
            normalized.put("places", response.getOrDefault("places", List.of()));
            cache.put(key, normalized);
            return normalized;
        } catch (Exception e) {
            return Map.of("status", "ERROR", "query", query, "message", safeMessage(e));
        }
    }

    public Map<String, Object> computeRoute(double originLat, double originLng,
                                            double destinationLat, double destinationLng,
                                            String mode, String departureTime) {
        if (!enabled()) return Map.of("status", "NOT_CONFIGURED");
        String travelMode = "TRANSIT".equalsIgnoreCase(mode) ? "TRANSIT" : "DRIVE";
        String key = "route:" + originLat + "," + originLng + ":" + destinationLat + "," + destinationLng + ":" + travelMode + ":" + (departureTime == null ? "now" : departureTime);
        Map<String, Object> cached = cache.getIfPresent(key);
        if (cached != null) return cached;

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("origin", Map.of("location", Map.of("latLng", Map.of("latitude", originLat, "longitude", originLng))));
            body.put("destination", Map.of("location", Map.of("latLng", Map.of("latitude", destinationLat, "longitude", destinationLng))));
            body.put("travelMode", travelMode);
            body.put("languageCode", "en");
            body.put("units", "METRIC");
            body.put("computeAlternativeRoutes", true);
            if ("TRANSIT".equals(travelMode)) {
                body.put("transitPreferences", Map.of("allowedTravelModes", List.of("BUS", "TRAIN", "RAIL"), "routingPreference", "FEWER_TRANSFERS"));
                if (departureTime != null && !departureTime.isBlank()) body.put("departureTime", departureTime);
            } else {
                body.put("routingPreference", "TRAFFIC_AWARE");
            }
            String fields = "routes.duration,routes.distanceMeters,routes.localizedValues,routes.legs.steps.transitDetails,routes.legs.steps.travelMode,routes.legs.steps.startLocation,routes.legs.steps.endLocation,routes.legs.steps.localizedValues,routes.travelAdvisory";
            Map<String, Object> response = client.post().uri(routesUrl)
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", fields)
                    .body(body)
                    .retrieve().body(Map.class);
            if (response == null) response = Map.of();
            Map<String, Object> normalized = new LinkedHashMap<>();
            normalized.put("status", "LIVE");
            normalized.put("queriedAt", OffsetDateTime.now(ZoneOffset.UTC).toString());
            normalized.put("travelMode", travelMode);
            normalized.put("routes", response.getOrDefault("routes", List.of()));
            cache.put(key, normalized);
            return normalized;
        } catch (Exception e) {
            return Map.of("status", "ERROR", "travelMode", travelMode, "message", safeMessage(e));
        }
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) return "External provider request failed";
        return message.substring(0, Math.min(message.length(), 200));
    }

}
