package com.ampara.tourism.integration;

import java.time.Instant;

public record ExternalDataResponse(
        ExternalDataSource source,
        String status,
        boolean live,
        Instant fetchedAt,
        String message,
        Object data
) {}
