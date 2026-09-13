package com.ampara.tourism.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/integrations")
public class ExternalDataIntegrationController {

    private final ExternalDataIntegrationService service;

    public ExternalDataIntegrationController(ExternalDataIntegrationService service) {
        this.service = service;
    }

    @GetMapping("/weather")
    public ResponseEntity<ExternalDataResponse> weather(
            @RequestParam @Size(max = 80) String town) {
        return ResponseEntity.ok(service.weather(town));
    }

    @GetMapping("/bus-timetable")
    public ResponseEntity<ExternalDataResponse> busTimetable(
            @RequestParam @Size(max = 80) String from,
            @RequestParam @Size(max = 80) String to) {
        return ResponseEntity.ok(service.busTimetable(from, to));
    }

    @GetMapping("/timekeeper")
    public ResponseEntity<ExternalDataResponse> timeKeeper(
            @RequestParam @Size(max = 80) String from,
            @RequestParam @Size(max = 80) String to) {
        return ResponseEntity.ok(service.timeKeeper(from, to));
    }

    @GetMapping("/directions")
    public ResponseEntity<ExternalDataResponse> directions(
            @RequestParam @Size(max = 200) String origin,
            @RequestParam @Size(max = 200) String destination) {
        return ResponseEntity.ok(service.googleDirections(origin, destination));
    }
}
