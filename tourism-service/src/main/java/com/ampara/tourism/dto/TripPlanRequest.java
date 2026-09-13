package com.ampara.tourism.dto;

import java.time.LocalDate;
import java.util.List;

public record TripPlanRequest(
        String town,
        String origin,
        String destination,
        LocalDate startDate,
        Integer days,
        Integer travelers,
        String budget,
        List<String> interests,
        String foodPreference,
        String transportPreference,
        String message
) {}
