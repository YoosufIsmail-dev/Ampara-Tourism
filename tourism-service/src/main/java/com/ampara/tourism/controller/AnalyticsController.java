package com.ampara.tourism.controller;

import com.ampara.tourism.dto.PlaceViewRequest;
import com.ampara.tourism.entity.PlaceView;
import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.PlaceViewRepository;
import com.ampara.tourism.repository.TouristPlaceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Records place views from the app (public) and exposes an aggregated
 * analytics dashboard for admins: most-visited places, category/town/language
 * breakdowns over a configurable time window.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final PlaceViewRepository placeViewRepository;
    private final TouristPlaceRepository placeRepository;

    public AnalyticsController(PlaceViewRepository placeViewRepository, TouristPlaceRepository placeRepository) {
        this.placeViewRepository = placeViewRepository;
        this.placeRepository = placeRepository;
    }

    /** Call this whenever a tourist opens a place's detail page - powers the dashboard below. */
    @PostMapping("/view")
    @ResponseStatus(HttpStatus.CREATED)
    public void logView(@Valid @RequestBody PlaceViewRequest request) {
        TouristPlace place = placeRepository.findById(request.getPlaceId()).orElse(null);
        PlaceView view = new PlaceView(
                request.getPlaceId(),
                place != null ? place.getName() : null,
                place != null ? place.getCategory() : null,
                place != null ? place.getTown() : null,
                request.getLanguage(),
                request.getSource()
        );
        placeViewRepository.save(view);
    }

    /** Admin dashboard: most-visited places, categories, towns and languages over the last N days (default 30). */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("windowDays", days);
        response.put("totalViews", placeViewRepository.findByViewedAtAfter(since).size());
        response.put("topPlaces", placeViewRepository.topPlaces(since));
        response.put("topCategories", placeViewRepository.topCategories(since));
        response.put("topTowns", placeViewRepository.topTowns(since));
        response.put("languageBreakdown", placeViewRepository.languageBreakdown(since));
        return response;
    }

    /** View history for a single place (admin). */
    @GetMapping("/place/{placeId}")
    public Object placeHistory(@PathVariable Long placeId) {
        return placeViewRepository.findByPlaceId(placeId);
    }
}
