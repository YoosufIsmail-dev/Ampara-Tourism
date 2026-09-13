package com.ampara.tourism.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;

/**
 * Server-side gateway for all external travel data.
 *
 * Frontend calls our /api/integrations/* endpoints only.
 * API keys and external URLs remain on the server.
 */
@Service
public class ExternalDataIntegrationService {

    private final RestClient restClient;
    private final ExternalDataCache cache;

    @Value("${integration.google.base-url:https://maps.googleapis.com}")
    private String googleBaseUrl;

    @Value("${integration.google.api-key:}")
    private String googleApiKey;

    @Value("${integration.meteo.base-url:https://meteo.gov.lk}")
    private String meteoBaseUrl;

    @Value("${integration.meteo.weather-path:}")
    private String meteoWeatherPath;

    @Value("${integration.bustimetable.base-url:https://bustimetable.lk}")
    private String busTimetableBaseUrl;

    @Value("${integration.timekeeper.base-url:https://www.timekeeper.lk}")
    private String timeKeeperBaseUrl;

    public ExternalDataIntegrationService(ExternalDataCache cache,
                                          @Value("${integration.http.connect-timeout-ms:5000}") int connectTimeoutMs,
                                          @Value("${integration.http.read-timeout-ms:10000}") int readTimeoutMs) {
        this.cache = cache;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, connectTimeoutMs)))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofMillis(Math.max(2000, readTimeoutMs)));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public ExternalDataResponse weather(String town) {
        String key = "weather:" + town.toLowerCase();
        Object cached = cache.get(key);
        if (cached != null) {
            return new ExternalDataResponse(
                    ExternalDataSource.METEOROLOGY, "CACHE", false,
                    Instant.now(), "Cached official-source response", cached);
        }

        // The exact Department of Meteorology endpoint can be configured via
        // integration.meteo.weather-path. We intentionally do not invent one.
        String path = meteoWeatherPath;
        if (path.isBlank()) {
            return fallback(ExternalDataSource.METEOROLOGY,
                    "Weather endpoint not configured; open official source.",
                    Map.of("source", meteoBaseUrl + "/"));
        }

        try {
            Object data = restClient.get()
                    .uri(buildUri(meteoBaseUrl, path, "town", town))
                    .retrieve()
                    .body(Object.class);
            cache.put(key, data, Duration.ofMinutes(10));
            return new ExternalDataResponse(
                    ExternalDataSource.METEOROLOGY, "OFFICIAL", true,
                    Instant.now(), "Fresh weather response", data);
        } catch (Exception ex) {
            return fallback(ExternalDataSource.METEOROLOGY,
                    "Official weather source unavailable; use last known/fallback data.",
                    Map.of("source", meteoBaseUrl + "/"));
        }
    }

    public ExternalDataResponse busTimetable(String from, String to) {
        return scheduledSource(
                ExternalDataSource.BUS_TIMETABLE,
                "bus:" + from.toLowerCase() + ":" + to.toLowerCase(),
                busTimetableBaseUrl + "/",
                "Published timetable; not live GPS.");
    }

    public ExternalDataResponse timeKeeper(String from, String to) {
        return scheduledSource(
                ExternalDataSource.TIMEKEEPER,
                "timekeeper:" + from.toLowerCase() + ":" + to.toLowerCase(),
                timeKeeperBaseUrl + "/",
                "Published timetable; verify before travel.");
    }

    public ExternalDataResponse googleDirections(String origin, String destination) {
        String key = "directions:" + origin + ":" + destination;
        Object cached = cache.get(key);
        if (cached != null) {
            return new ExternalDataResponse(
                    ExternalDataSource.GOOGLE_MAPS, "CACHE", false,
                    Instant.now(), "Cached route response", cached);
        }

        if (googleApiKey == null || googleApiKey.isBlank()) {
            return fallback(ExternalDataSource.GOOGLE_MAPS,
                    "Google Maps API key is not configured; open Google Maps navigation.",
                    Map.of("baseUrl", googleBaseUrl, "origin", origin, "destination", destination));
        }

        try {
            String uri = googleBaseUrl.replaceAll("/+$", "")
                    + "/maps/api/directions/json?origin="
                    + java.net.URLEncoder.encode(origin == null ? "" : origin, java.nio.charset.StandardCharsets.UTF_8)
                    + "&destination="
                    + java.net.URLEncoder.encode(destination == null ? "" : destination, java.nio.charset.StandardCharsets.UTF_8)
                    + "&key="
                    + java.net.URLEncoder.encode(googleApiKey, java.nio.charset.StandardCharsets.UTF_8);

            Object data = restClient.get().uri(uri).retrieve().body(Object.class);
            cache.put(key, data, Duration.ofMinutes(5));
            return new ExternalDataResponse(
                    ExternalDataSource.GOOGLE_MAPS, "OFFICIAL", true,
                    Instant.now(), "Fresh Google route response", data);
        } catch (Exception ex) {
            return fallback(ExternalDataSource.GOOGLE_MAPS,
                    "Google route service unavailable; use Google Maps navigation.",
                    Map.of("baseUrl", googleBaseUrl, "origin", origin, "destination", destination));
        }
    }

    private String buildUri(String baseUrl, String path, String queryName, String queryValue) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        String suffix = path == null ? "" : path.trim();
        if (base.endsWith("/") && suffix.startsWith("/")) suffix = suffix.substring(1);
        else if (!base.endsWith("/") && !suffix.isEmpty() && !suffix.startsWith("/")) suffix = "/" + suffix;
        String encoded = java.net.URLEncoder.encode(queryValue == null ? "" : queryValue, java.nio.charset.StandardCharsets.UTF_8);
        return base + suffix + "?" + queryName + "=" + encoded;
    }

    private ExternalDataResponse scheduledSource(
            ExternalDataSource source, String key, String sourceUrl, String message) {
        Object cached = cache.get(key);
        if (cached != null) {
            return new ExternalDataResponse(
                    source, "CACHE", false, Instant.now(), message, cached);
        }
        return fallback(source, message, Map.of("source", sourceUrl));
    }

    private ExternalDataResponse fallback(
            ExternalDataSource source, String message, Object data) {
        return new ExternalDataResponse(
                source, "FALLBACK", false, Instant.now(), message, data);
    }
}
