package com.ampara.tourism.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ampara.tourism.entity.FoodItem;
import com.ampara.tourism.entity.Hotel;
import com.ampara.tourism.entity.HotelRoom;
import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.FoodItemRepository;
import com.ampara.tourism.repository.HotelRepository;
import com.ampara.tourism.repository.HotelRoomRepository;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.ampara.tourism.service.GeoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/places")
public class NearbyRecommendationController {
    private final TouristPlaceRepository places;
    private final HotelRepository hotels;
    private final FoodItemRepository foods;
    private final HotelRoomRepository rooms;
    private final Cache<String, Map<String,Object>> responseCache;

    public NearbyRecommendationController(TouristPlaceRepository places, HotelRepository hotels,
                                          FoodItemRepository foods, HotelRoomRepository rooms,
                                          @Value("${app.cache.nearby-ttl-seconds:60}") long ttlSeconds,
                                          @Value("${app.cache.nearby-max-entries:1000}") long maxEntries) {
        this.places = places; this.hotels = hotels; this.foods = foods; this.rooms = rooms;
        this.responseCache = Caffeine.newBuilder()
                .maximumSize(Math.max(100, maxEntries))
                .expireAfterWrite(Math.max(10, ttlSeconds), java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    @GetMapping("/{placeId}/nearby")
    public ResponseEntity<Map<String,Object>> nearby(@PathVariable Long placeId,
                                                       @RequestParam(defaultValue = "15") double radiusKm,
                                                       @RequestParam(defaultValue = "5") int limit) {
        double safeRadius = Math.min(Math.max(radiusKm, 0.5), 50.0);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        String cacheKey = placeId + ":" + safeRadius + ":" + safeLimit;
        Map<String,Object> cached = responseCache.getIfPresent(cacheKey);
        if (cached != null) return ResponseEntity.ok(cached);

        Optional<TouristPlace> found = places.findById(placeId);
        if (found.isEmpty()) return ResponseEntity.notFound().build();
        TouristPlace place = found.get();
        if (place.getLatitude() == null || place.getLongitude() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "This tourist place has no coordinates."));
        }

        // Bounding-box prefilter keeps large tables out of JVM memory.
        double latDelta = safeRadius / 111.32;
        double lonFactor = Math.max(0.2, Math.cos(Math.toRadians(place.getLatitude())));
        double lonDelta = safeRadius / (111.32 * lonFactor);
        double minLat = place.getLatitude() - latDelta, maxLat = place.getLatitude() + latDelta;
        double minLon = place.getLongitude() - lonDelta, maxLon = place.getLongitude() + lonDelta;

        List<Hotel> candidateHotels = hotels.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon);
        List<Map<String,Object>> hotelResults = candidateHotels.stream()
                .map(h -> withDistance(h, GeoUtil.distanceKm(place.getLatitude(), place.getLongitude(), h.getLatitude(), h.getLongitude())))
                .filter(m -> (double)m.get("distanceKm") <= safeRadius)
                .sorted(Comparator.comparingDouble(m -> (double)m.get("distanceKm")))
                .limit(safeLimit)
                .collect(Collectors.toList());

        // Foods with GPS use the same DB-level bounding box. Foods without GPS are
        // resolved at runtime from GPS-enabled hotels in the same town.
        Map<String, double[]> townFallbackCoordinates = new HashMap<>();
        if (place.getTown() != null) {
            hotels.findByTownIgnoreCaseAndLatitudeIsNotNullAndLongitudeIsNotNull(place.getTown()).forEach(h -> {
                double[] acc = townFallbackCoordinates.computeIfAbsent(h.getTown().trim().toLowerCase(Locale.ROOT), k -> new double[3]);
                acc[0] += h.getLatitude(); acc[1] += h.getLongitude(); acc[2] += 1;
            });
        }

        List<FoodItem> candidateFoods = foods.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon);
        // Add town-level items without GPS; duplicates are removed by ID below.
        List<FoodItem> townFoods = place.getTown() == null ? List.of() : foods.findByTownIgnoreCase(place.getTown());
        Map<Long, FoodItem> uniqueFoods = new LinkedHashMap<>();
        candidateFoods.forEach(f -> uniqueFoods.put(f.getId(), f));
        townFoods.forEach(f -> { if (f.getLatitude() == null || f.getLongitude() == null) uniqueFoods.put(f.getId(), f); });

        List<Map<String,Object>> foodResults = uniqueFoods.values().stream()
                .map(f -> {
                    Double lat = f.getLatitude(), lon = f.getLongitude(); boolean fallback = false;
                    if ((lat == null || lon == null) && f.getTown() != null) {
                        double[] acc = townFallbackCoordinates.get(f.getTown().trim().toLowerCase(Locale.ROOT));
                        if (acc != null && acc[2] > 0) { lat = acc[0] / acc[2]; lon = acc[1] / acc[2]; fallback = true; }
                    }
                    if (lat == null || lon == null) return null;
                    Map<String,Object> m = withDistance(f, GeoUtil.distanceKm(place.getLatitude(), place.getLongitude(), lat, lon));
                    m.put("coordinateSource", fallback ? "town-fallback" : "item-gps");
                    m.put("resolvedLatitude", lat); m.put("resolvedLongitude", lon);
                    return m;
                })
                .filter(Objects::nonNull)
                .filter(m -> (double)m.get("distanceKm") <= safeRadius)
                .sorted(Comparator.comparingDouble(m -> (double)m.get("distanceKm")))
                .limit(safeLimit)
                .collect(Collectors.toList());

        List<Long> nearbyHotelIds = hotelResults.stream().map(m -> ((Hotel)m.get("entity")).getId()).filter(Objects::nonNull).toList();
        Map<Long, List<HotelRoom>> roomsByHotel = nearbyHotelIds.isEmpty() ? Collections.emptyMap() :
                rooms.findByHotelIdIn(nearbyHotelIds).stream().collect(Collectors.groupingBy(r -> r.getHotel().getId()));

        List<Map<String,Object>> hotelWithRooms = hotelResults.stream().map(m -> {
            Hotel h = (Hotel)m.get("entity"); Map<String,Object> out = new LinkedHashMap<>();
            out.put("id", h.getId()); out.put("name", h.getName()); out.put("town", h.getTown());
            out.put("address", h.getAddress()); out.put("latitude", h.getLatitude()); out.put("longitude", h.getLongitude());
            out.put("pricePerNight", h.getPricePerNight()); out.put("rating", h.getRating()); out.put("imageUrl", h.getImageUrl());
            out.put("description", h.getDescription()); out.put("distanceKm", m.get("distanceKm"));
            out.put("rooms", roomsByHotel.getOrDefault(h.getId(), Collections.emptyList()).stream().map(r -> {
                Map<String,Object> rm = new LinkedHashMap<>(); rm.put("id",r.getId()); rm.put("roomType",r.getRoomType());
                rm.put("pricePerNight",r.getPricePerNight()); rm.put("capacity",r.getCapacity()); rm.put("available",r.getAvailable()); rm.put("amenities",r.getAmenities()); return rm;
            }).collect(Collectors.toList()));
            return out;
        }).toList();

        List<Map<String,Object>> foodOut = foodResults.stream().map(m -> {
            FoodItem f=(FoodItem)m.get("entity"); Map<String,Object> out=new LinkedHashMap<>();
            out.put("id",f.getId()); out.put("name",f.getName()); out.put("restaurantName",f.getRestaurantName()); out.put("town",f.getTown());
            out.put("category",f.getCategory()); out.put("price",f.getPrice()); out.put("description",f.getDescription()); out.put("imageUrl",f.getImageUrl());
            out.put("vegetarian",f.getVegetarian()); out.put("contactNumber",f.getContactNumber()); out.put("latitude",m.get("resolvedLatitude"));
            out.put("longitude",m.get("resolvedLongitude")); out.put("distanceKm",m.get("distanceKm")); out.put("coordinateSource",m.get("coordinateSource")); return out;
        }).toList();

        Map<String,Object> result=new LinkedHashMap<>();
        Map<String,Object> placeOut = new LinkedHashMap<>();
        placeOut.put("id", place.getId()); placeOut.put("name", place.getName()); placeOut.put("town", place.getTown());
        placeOut.put("latitude", place.getLatitude()); placeOut.put("longitude", place.getLongitude());
        result.put("place", placeOut);
        result.put("radiusKm", safeRadius); result.put("hotels", hotelWithRooms); result.put("foods", foodOut);
        result.put("hotelCount", hotelWithRooms.size()); result.put("foodCount", foodOut.size());
        responseCache.put(cacheKey, result);
        return ResponseEntity.ok(result);
    }

    private Map<String,Object> withDistance(Hotel h, double d) { Map<String,Object> m=new HashMap<>(); m.put("entity",h); m.put("distanceKm",round(d)); return m; }
    private Map<String,Object> withDistance(FoodItem f, double d) { Map<String,Object> m=new HashMap<>(); m.put("entity",f); m.put("distanceKm",round(d)); return m; }
    private double round(double d) { return Math.round(d*100.0)/100.0; }
}
