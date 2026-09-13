package com.ampara.tourism.service;

import com.ampara.tourism.dto.TripPlanRequest;
import com.ampara.tourism.dto.TripPlanResponse;
import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.live.GoogleMapsLiveService;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.ampara.tourism.repository.TransportRouteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LiveTripPlannerService {
    private static final ZoneId SRI_LANKA = ZoneId.of("Asia/Colombo");

    private final ChatClient chatClient;
    private final TouristPlaceRepository touristPlaceRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final WeatherService weatherService;
    private final GoogleMapsLiveService googleMaps;
    private final ObjectMapper objectMapper;
    private final BusTimetableSourceService busTimetableSource;

    public LiveTripPlannerService(ChatClient chatClient,
                                  TouristPlaceRepository touristPlaceRepository,
                                  TransportRouteRepository transportRouteRepository,
                                  WeatherService weatherService,
                                  GoogleMapsLiveService googleMaps,
                                  ObjectMapper objectMapper,
                                  BusTimetableSourceService busTimetableSource) {
        this.chatClient = chatClient;
        this.touristPlaceRepository = touristPlaceRepository;
        this.transportRouteRepository = transportRouteRepository;
        this.weatherService = weatherService;
        this.googleMaps = googleMaps;
        this.objectMapper = objectMapper;
        this.busTimetableSource = busTimetableSource;
    }

    public TripPlanResponse plan(TripPlanRequest request) {
        String town = Optional.ofNullable(request.town()).filter(s -> !s.isBlank()).orElse("Ampara");
        List<TouristPlace> attractions = touristPlaceRepository.findByTownIgnoreCase(town);
        if (attractions.isEmpty()) attractions = touristPlaceRepository.findAll();

        List<TouristPlace> candidates = rankCandidates(attractions, request.interests());
        if (candidates.size() > 8) candidates = candidates.subList(0, 8);

        Map<String, Object> rawWeather = request.startDate() != null ? weatherService.getForecast(town + ",LK") : weatherService.getCurrentWeather(town + ",LK");
        Map<String, Object> weather = compactWeather(rawWeather);
        List<Map<String, Object>> livePlaces = new ArrayList<>();
        for (TouristPlace a : candidates) {
            String query = a.getName() + ", " + town + ", Sri Lanka";
            livePlaces.add(Map.of(
                    "attraction", a.getName(),
                    "category", a.getCategory() == null ? "" : a.getCategory(),
                    "live", googleMaps.searchPlace(query, a.getLatitude(), a.getLongitude())
            ));
        }

        Map<String, Object> liveOrigin = resolvePoint(request.origin(), town);
        Map<String, Object> liveDestination = resolvePoint(request.destination(), town);
        Map<String, Object> liveTransit = Map.of("status", "NOT_REQUESTED");
        List<Map<String, Object>> scheduledTransport = transportRouteRepository.findAll().stream()
                .filter(t -> containsIgnoreCase(t.getOrigin(), town) || containsIgnoreCase(t.getDestination(), town))
                .limit(12)
                .map(t -> { Map<String,Object> m = new LinkedHashMap<>(); m.put("type",t.getType()); m.put("routeName",t.getRouteName()); m.put("origin",t.getOrigin()); m.put("destination",t.getDestination()); m.put("departureTimes",t.getDepartureTimes()); m.put("frequency",t.getFrequency()); m.put("fare",t.getFare()); m.put("operator",t.getOperatorName()); m.put("status","SCHEDULED_DB"); return m; })
                .toList();
        Map<String, Object> publishedBusTimetable = busTimetableSource.search(request.origin(), request.destination(), town);
        if ("BUS".equalsIgnoreCase(request.transportPreference()) ||
                "TRANSIT".equalsIgnoreCase(request.transportPreference())) {
            PointPair pair = pointPair(liveOrigin, liveDestination, candidates);
            if (pair != null) {
                liveTransit = googleMaps.computeRoute(pair.origin().lat(), pair.origin().lng(), pair.destination().lat(), pair.destination().lng(),
                        "TRANSIT", request.startDate() == null ? null : request.startDate().atStartOfDay(SRI_LANKA).toOffsetDateTime().toString());
            }
        }

        Map<String, Object> dataStatus = new LinkedHashMap<>();
        dataStatus.put("weather", weatherStatus(weather));
        dataStatus.put("googlePlaces", googleMaps.enabled() ? "LIVE" : "NOT_CONFIGURED");
        dataStatus.put("googleRoutes", googleMaps.enabled() ? "LIVE_WHEN_SUPPORTED" : "NOT_CONFIGURED");
        dataStatus.put("transit", liveTransit.getOrDefault("status", "UNKNOWN"));
        dataStatus.put("busTimetable", publishedBusTimetable.getOrDefault("status", "UNKNOWN"));
        dataStatus.put("generatedAt", OffsetDateTime.now(SRI_LANKA).toString());

        String context = buildContext(request, town, candidates, weather, livePlaces, liveTransit, scheduledTransport, publishedBusTimetable, dataStatus);
        String plan = chatClient.prompt()
                .user(context)
                .call()
                .content();

        return new TripPlanResponse(plan, OffsetDateTime.now(SRI_LANKA),
                googleMaps.enabled(), googleMaps.enabled(),
                "LIVE".equals(liveTransit.get("status")), dataStatus);
    }

    private List<TouristPlace> rankCandidates(List<TouristPlace> attractions, List<String> interests) {
        if (interests == null || interests.isEmpty()) return attractions.stream().limit(8).toList();
        Set<String> wanted = interests.stream().filter(Objects::nonNull).map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        return attractions.stream().sorted((a, b) -> Integer.compare(score(b, wanted), score(a, wanted))).toList();
    }

    private int score(TouristPlace a, Set<String> wanted) {
        String hay = ((a.getName() == null ? "" : a.getName()) + " " +
                (a.getCategory() == null ? "" : a.getCategory()) + " " +
                (a.getDescriptionEn() == null ? "" : a.getDescriptionEn()) + " " +
                String.join(" ", a.getActivities() == null ? List.of() : a.getActivities())).toLowerCase(Locale.ROOT);
        return (int) wanted.stream().filter(hay::contains).count();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolvePoint(String query, String fallbackTown) {
        if (query == null || query.isBlank()) return Map.of("status", "NOT_PROVIDED");
        return googleMaps.searchPlace(query + ", " + fallbackTown + ", Sri Lanka", null, null);
    }

    @SuppressWarnings("unchecked")
    private PointPair pointPair(Map<String, Object> origin, Map<String, Object> destination, List<TouristPlace> candidates) {
        Double oLat = locationLat(origin), oLng = locationLng(origin);
        Double dLat = locationLat(destination), dLng = locationLng(destination);
        if (oLat != null && oLng != null && dLat != null && dLng != null) return new PointPair(oLat, oLng, dLat, dLng);
        if (candidates.size() >= 2 && candidates.get(0).getLatitude() != null && candidates.get(0).getLongitude() != null
                && candidates.get(1).getLatitude() != null && candidates.get(1).getLongitude() != null) {
            return new PointPair(candidates.get(0).getLatitude(), candidates.get(0).getLongitude(), candidates.get(1).getLatitude(), candidates.get(1).getLongitude());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Double locationLat(Map<String, Object> root) {
        Object placesObj = root.get("places");
        if (!(placesObj instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof Map<?, ?> p)) return null;
        Object loc = p.get("location");
        if (!(loc instanceof Map<?, ?> m)) return null;
        Object v = m.get("latitude");
        return v instanceof Number n ? n.doubleValue() : null;
    }

    @SuppressWarnings("unchecked")
    private Double locationLng(Map<String, Object> root) {
        Object placesObj = root.get("places");
        if (!(placesObj instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof Map<?, ?> p)) return null;
        Object loc = p.get("location");
        if (!(loc instanceof Map<?, ?> m)) return null;
        Object v = m.get("longitude");
        return v instanceof Number n ? n.doubleValue() : null;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> compactWeather(Map<String, Object> raw) {
        if (raw == null) return Map.of("status", "UNKNOWN");
        if (!raw.containsKey("list")) return raw;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", raw.getOrDefault("status", "LIVE"));
        out.put("city", raw.get("city"));
        Object list = raw.get("list");
        if (list instanceof List<?> entries) out.put("forecastSamples", entries.stream().limit(12).toList());
        return out;
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && needle != null && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private String weatherStatus(Map<String, Object> weather) {
        if (weather.containsKey("main") || weather.containsKey("weather")) return "LIVE";
        return String.valueOf(weather.getOrDefault("status", "UNKNOWN"));
    }

    private String buildContext(TripPlanRequest request, String town, List<TouristPlace> candidates,
                                Map<String, Object> weather, List<Map<String, Object>> livePlaces,
                                Map<String, Object> liveTransit, List<Map<String, Object>> scheduledTransport, Map<String, Object> publishedBusTimetable, Map<String, Object> dataStatus) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("travelerRequest", request.message());
            root.put("town", town);
            root.put("startDate", request.startDate());
            root.put("days", request.days());
            root.put("travelers", request.travelers());
            root.put("budget", request.budget());
            root.put("interests", request.interests());
            root.put("foodPreference", request.foodPreference());
            root.put("transportPreference", request.transportPreference());
            root.put("generatedAtSriLanka", OffsetDateTime.now(SRI_LANKA).toString());
            root.put("weather", weather);
            root.put("dataStatus", dataStatus);
            root.put("candidateAttractions", candidates.stream().map(a -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", a.getName());
                item.put("category", a.getCategory());
                item.put("tourismType", a.getTourismType());
                item.put("description", a.getDescriptionEn());
                item.put("latitude", a.getLatitude());
                item.put("longitude", a.getLongitude());
                item.put("openingHoursFromDatabase", a.getOpeningHours());
                item.put("entryFee", a.getEntryFee());
                item.put("activities", a.getActivities());
                return item;
            }).toList());
            root.put("livePlacesAndHours", livePlaces);
            root.put("liveTransit", liveTransit);
            root.put("scheduledTransportFallback", scheduledTransport);
            root.put("publishedBusTimetable", publishedBusTimetable);
            return """
                    You are the live-data AI trip planner for a Sri Lanka tourism application.
                    Use ONLY the supplied candidate attractions and live provider data.
                    Never invent opening hours, travel times, bus departures, prices, ratings, or availability.
                    If a live field is missing or provider data is unavailable, explicitly label it as unknown/not verified.
                    Prefer places that are open during the proposed visit window. Do not schedule a place outside its live opening hours.
                    Minimize unnecessary travel and group nearby places. Respect the requested budget, interests, food and transport preferences.
                    Use the published BusTimetable.lk and TimeKeeper.lk timetables when they contain matching routes. Label both as 'scheduled, not live' and retain the source name for each departure. Never describe either source as live GPS availability. If the two sources disagree, do not choose silently; show the conflicting schedules and advise verification. If neither published nor database timetable data is available, say that bus timing is not verified.
                    Return a practical day-by-day itinerary with approximate times, places, travel notes, meals and a short live-data status section.
                    
                    DATA:
                    %s
                    """.formatted(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (Exception e) {
            return "Traveler request: " + request.message() + "\nTown: " + town;
        }
    }

    private record PointPair(double originLat, double originLng, double destinationLat, double destinationLng) {
        Point origin() { return new Point(originLat, originLng); }
        Point destination() { return new Point(destinationLat, destinationLng); }
    }
    private record Point(double lat, double lng) {}
}
