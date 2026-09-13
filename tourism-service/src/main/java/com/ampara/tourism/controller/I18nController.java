package com.ampara.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves UI translation bundles for the app's three supported languages:
 * English (en), Tamil (ta / தமிழ்), and Sinhala (si / සිංහල).
 *
 * These are static interface strings (button labels, nav items, etc). Tourist-place
 * specific content (name/description) is localized instead via
 * GET /api/places/{id}/localized?lang=en|ta|si
 */
@RestController
@RequestMapping("/api/i18n")
public class I18nController {

    private static final List<String> SUPPORTED = List.of("en", "ta", "si");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/languages")
    public Map<String, Object> languages() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("supported", List.of(
                Map.of("code", "en", "label", "English"),
                Map.of("code", "ta", "label", "தமிழ்"),
                Map.of("code", "si", "label", "සිංහල")
        ));
        response.put("default", "en");
        return response;
    }

    @GetMapping("/{lang}")
    public ResponseEntity<Map<String, String>> bundle(@PathVariable String lang) {
        String normalized = SUPPORTED.contains(lang.toLowerCase()) ? lang.toLowerCase() : "en";
        try (InputStream in = new ClassPathResource("i18n/messages_" + normalized + ".json").getInputStream()) {
            Map<String, String> strings = objectMapper.readValue(in, Map.class);
            return ResponseEntity.ok(strings);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
