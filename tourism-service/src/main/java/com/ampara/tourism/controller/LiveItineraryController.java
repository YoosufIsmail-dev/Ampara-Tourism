package com.ampara.tourism.controller;

import com.ampara.tourism.dto.TripPlanRequest;
import com.ampara.tourism.dto.TripPlanResponse;
import com.ampara.tourism.service.LiveTripPlannerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class LiveItineraryController {
    private final LiveTripPlannerService planner;

    public LiveItineraryController(LiveTripPlannerService planner) {
        this.planner = planner;
    }

    @PostMapping("/trip-plan")
    public TripPlanResponse plan(@Valid @RequestBody TripPlanRequest request) {
        return planner.plan(request);
    }
}
