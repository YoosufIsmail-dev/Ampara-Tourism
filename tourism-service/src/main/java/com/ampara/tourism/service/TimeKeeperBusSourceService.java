package com.ampara.tourism.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads TimeKeeper.lk published bus timetables. This is scheduled timetable
 * information, not live vehicle GPS/seat availability.
 */
@Service
public class TimeKeeperBusSourceService {
    private static final ZoneId SRI_LANKA = ZoneId.of("Asia/Colombo");
    private static final String SOURCE = "TimeKeeper.lk";
    private final String baseUrl;
    private static final Pattern ROUTE = Pattern.compile("(?i)(?:Available Routes from|Route:)\\s*([^\\n]+?)(?:\\s+to\\s+|\\s+-\\s+)([^\\n]+)");
    private static final Pattern DEPARTURE = Pattern.compile("(?i)([A-Za-z][A-Za-z .'-]{1,40})\\s+Departure:\\s*([0-2]?\\d:\\d{2})\\s+([A-Za-z][A-Za-z .'-]{1,40})\\s+Arrival:\\s*([0-2]?\\d:\\d{2})");
    private static final Pattern PROVIDER_LINE = Pattern.compile("(?i)\\b(SLTB|K-\\d+|C-\\d+|[A-Za-z][A-Za-z0-9 .'-]{2,35}\\s+\\d{1,3})\\b");

    private final long cacheSeconds;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public TimeKeeperBusSourceService(@Value("${app.timekeeper.cache-seconds:600}") long cacheSeconds,
                                      @Value("${app.timekeeper.base-url:https://www.timekeeper.lk}") String baseUrl) {
        this.cacheSeconds = Math.max(60, cacheSeconds);
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    public Map<String, Object> search(String origin, String destination, String town) {
        String from = safe(origin);
        String to = safe(destination);
        String key = (from + "|" + to + "|" + safe(town)).toLowerCase(Locale.ROOT);
        CacheEntry hit = cache.get(key);
        if (hit != null && Duration.between(hit.fetchedAt(), OffsetDateTime.now(SRI_LANKA)).getSeconds() < cacheSeconds) return hit.payload();

        if (from.isBlank() || to.isBlank()) return unavailable("Enter both From and To to search TimeKeeper.lk.");
        String url = baseUrl + "/normalway/?from=" + encode(from) + "&to=" + encode(to);
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 TourismService/1.0").timeout(12000).followRedirects(true).get();
            String text = doc.body().text().replaceAll("\\s+", " ").trim();
            List<Map<String, Object>> rows = extract(text, from, to, url);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("status", rows.isEmpty() ? "NO_MATCH" : "SCHEDULED");
            payload.put("source", SOURCE);
            payload.put("sourceUrl", url);
            payload.put("fetchedAt", OffsetDateTime.now(SRI_LANKA).toString());
            payload.put("isLiveVehicleData", false);
            payload.put("notice", "Published timetable; departure/arrival times may change. Verify before travel. Not live GPS availability.");
            payload.put("routes", rows);
            cache.put(key, new CacheEntry(OffsetDateTime.now(SRI_LANKA), payload));
            return payload;
        } catch (Exception e) {
            return unavailable("TimeKeeper.lk could not be refreshed right now.");
        }
    }

    private List<Map<String, Object>> extract(String text, String from, String to, String url) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Matcher m = DEPARTURE.matcher(text);
        while (m.find() && rows.size() < 120) {
            String rowFrom = m.group(1).trim();
            String departure = m.group(2).trim();
            String rowTo = m.group(3).trim();
            String arrival = m.group(4).trim();
            int start = Math.max(0, m.start() - 45);
            String prefix = text.substring(start, m.start());
            String provider = findProvider(prefix);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("origin", rowFrom.isBlank() ? from : rowFrom);
            row.put("destination", rowTo.isBlank() ? to : rowTo);
            row.put("departure", departure);
            row.put("arrival", arrival);
            row.put("arrivalOrRoute", rowFrom + " → " + rowTo + " (arrival " + arrival + ")");
            row.put("operator", provider);
            row.put("busNumber", provider);
            row.put("source", SOURCE);
            row.put("sourceUrl", url);
            row.put("status", "SCHEDULED");
            rows.add(row);
        }
        if (rows.isEmpty()) {
            Matcher route = ROUTE.matcher(text);
            String inferredFrom = from;
            String inferredTo = to;
            if (route.find()) {
                inferredFrom = route.group(1).trim();
                inferredTo = route.group(2).trim();
            }
            if (text.toLowerCase(Locale.ROOT).contains("available routes")) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("origin", inferredFrom);
                row.put("destination", inferredTo);
                row.put("departure", "See published page");
                row.put("arrivalOrRoute", "Published route timetable");
                row.put("operator", "");
                row.put("busNumber", "");
                row.put("source", SOURCE);
                row.put("sourceUrl", url);
                row.put("status", "SCHEDULED");
                rows.add(row);
            }
        }
        return rows;
    }

    private String findProvider(String prefix) {
        Matcher m = PROVIDER_LINE.matcher(prefix);
        String found = "";
        while (m.find()) found = m.group(1).trim();
        return found;
    }

     private Map<String, Object> unavailable(String message) {
        payload = new LinkedHashMap<>();
        payload.put("status", "SOURCE_UNAVAILABLE");
        payload.put("source", SOURCE);
        payload.put("sourceUrl", baseUrl);
        payload.put("isLiveVehicleData", false);
        payload.put("message", message);
        payload.put("routes", List.of());
        return payload;
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
    private String safe(String s) { return s == null ? "" : s.trim(); }
    private record CacheEntry(OffsetDateTime fetchedAt, Map<String, Object> payload) {}
}
