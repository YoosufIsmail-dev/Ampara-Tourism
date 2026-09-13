package com.ampara.tourism.dto;

public record AdminStatsResponse(long attractionCount, long hotelCount, long bookingCount, long userCount,
                                  long placeCount, long reviewCount) {
}
