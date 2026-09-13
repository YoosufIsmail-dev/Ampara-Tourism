package com.ampara.tourism.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record TripPlanResponse(
        String plan,
        OffsetDateTime generatedAt,
        boolean liveMapsEnabled,
        boolean livePlacesEnabled,
        boolean liveTransitEnabled,
        Map<String, Object> dataStatus
) {}
