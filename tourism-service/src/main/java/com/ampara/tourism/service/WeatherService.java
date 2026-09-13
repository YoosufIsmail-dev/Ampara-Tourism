package com.ampara.tourism.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;

import java.util.Map;

@Service
public class WeatherService {

    @Value("${app.weather.api-key:}")
    private String apiKey;

    @Value("${app.weather.base-url:https://api.openweathermap.org/data/2.5/weather}")
    private String baseUrl;

    @Value("${app.weather.forecast-url:https://api.openweathermap.org/data/2.5/forecast}")
    private String forecastUrl;

    private final RestClient restClient;

    public WeatherService(
            @Value("${integration.http.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${integration.http.read-timeout-ms:10000}") int readTimeoutMs) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, connectTimeoutMs)))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofMillis(Math.max(2000, readTimeoutMs)));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public Map<String, Object> getCurrentWeather(String place) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of(
                    "place", place,
                    "status", "weather API key not configured",
                    "hint", "Set WEATHER_API_KEY (OpenWeatherMap) to enable live data"
            );
        }

        try {
            return restClient.get()
                    .uri(baseUrl + "?q={place}&appid={key}&units=metric", place, apiKey)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            return Map.of("place", place, "error", "Failed to fetch weather: " + safe(e));
        }
    }

    public Map<String, Object> getForecast(String place) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("place", place, "status", "weather API key not configured");
        }
        try {
            return restClient.get()
                    .uri(forecastUrl + "?q={place}&appid={key}&units=metric", place, apiKey)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            return Map.of("place", place, "error", "Failed to fetch weather forecast: " + safe(e));
        }
    }

    private String safe(Exception e) {
        String s = e.getMessage();
        return s == null ? "provider request failed" : s.substring(0, Math.min(250, s.length()));
    }
}
