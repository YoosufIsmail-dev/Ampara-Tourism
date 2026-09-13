package com.ampara.tourism.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads the public BusTimetable.lk pages on demand and exposes the published
 * timetable as scheduled data. It is deliberately NOT labelled as GPS/live bus
 * location data because the source publishes schedules, not vehicle telemetry.
 */
@Service
public class BusTimetableSourceService {
    private static final ZoneId SRI_LANKA = ZoneId.of("Asia/Colombo");
    private static final String SOURCE_NAME = "BusTimetable.lk";
    private static final String DEFAULT_URL = "https://bustimetable.lk/makumbura-highway-bus-time-table/";

    private final String baseUrl;
    private final long cacheSeconds;
    private final TimeKeeperBusSourceService timeKeeper;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public BusTimetableSourceService(
            @Value("${app.bus-timetable.base-url:https://bustimetable.lk}") String baseUrl,
            @Value("${app.bus-timetable.cache-seconds:600}") long cacheSeconds,
            TimeKeeperBusSourceService timeKeeper) {
        this.timeKeeper = timeKeeper;
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.cacheSeconds = Math.max(60, cacheSeconds);
    }

    public Map<String, Object> search(String origin, String destination, String town) {
        String key = String.join("|", safe(origin), safe(destination), safe(town)).toLowerCase(Locale.ROOT);
        CacheEntry hit = cache.get(key);
        if (hit != null && Duration.between(hit.fetchedAt(), OffsetDateTime.now(SRI_LANKA)).getSeconds() < cacheSeconds) {
            return hit.payload();
        }

        try {
            List<Map<String, Object>> results = new ArrayList<>();
            Set<String> pageUrls = discoverCandidatePages(origin, destination, town);
            if (pageUrls.isEmpty()) pageUrls.add(DEFAULT_URL);
            for (String url : pageUrls) {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 TourismService/1.0")
                        .timeout(12000)
                        .followRedirects(true)
                        .get();
                results.addAll(extractTables(doc, url, origin, destination, town));
            }
            // De-duplicate the same published row coming from linked/index pages.
            LinkedHashMap<String, Map<String, Object>> unique = new LinkedHashMap<>();
            for (Map<String, Object> row : results) {
                String id = row.get("origin") + "|" + row.get("destination") + "|" + row.get("departure") + "|" + row.get("busNumber");
                unique.putIfAbsent(id, row);
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("status", unique.isEmpty() ? "NO_MATCH" : "SCHEDULED");
            payload.put("source", SOURCE_NAME);
            payload.put("sourceUrl", pageUrls.iterator().next());
            payload.put("fetchedAt", OffsetDateTime.now(SRI_LANKA).toString());
            payload.put("isLiveVehicleData", false);
            payload.put("notice", "Published timetable; verify with operator before travel. Not live GPS availability.");
            payload.put("routes", new ArrayList<>(unique.values()));
            cache.put(key, new CacheEntry(OffsetDateTime.now(SRI_LANKA), payload));
            return payload;
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "SOURCE_UNAVAILABLE");
            error.put("source", SOURCE_NAME);
            error.put("sourceUrl", DEFAULT_URL);
            error.put("isLiveVehicleData", false);
            error.put("message", "Timetable source could not be refreshed right now.");
            return error;
        }
    }

    /** Combines BusTimetable.lk and TimeKeeper.lk into one transport response. */
    public Map<String, Object> searchAllSources(String origin, String destination, String town) {
        Map<String, Object> primary = search(origin, destination, town);
        Map<String, Object> timeKeeperPayload = timeKeeper.search(origin, destination, town);
        List<Map<String, Object>> routes = new ArrayList<>();
        Object primaryRoutes = primary.get("routes");
        if (primaryRoutes instanceof List<?> list) for (Object item : list) if (item instanceof Map<?, ?> map) routes.add(new LinkedHashMap<>((Map) map));
        Object tkRoutes = timeKeeperPayload.get("routes");
        if (tkRoutes instanceof List<?> list) for (Object item : list) if (item instanceof Map<?, ?> map) routes.add(new LinkedHashMap<>((Map) map));
        LinkedHashMap<String, Map<String, Object>> unique = new LinkedHashMap<>();
        for (Map<String, Object> row : routes) {
            String key = String.join("|", safe(String.valueOf(row.getOrDefault("source", ""))), safe(String.valueOf(row.getOrDefault("origin", ""))), safe(String.valueOf(row.getOrDefault("destination", ""))), safe(String.valueOf(row.getOrDefault("departure", ""))), safe(String.valueOf(row.getOrDefault("busNumber", "")))).toLowerCase(Locale.ROOT);
            unique.putIfAbsent(key, row);
        }
        Map<String, Object> combined = new LinkedHashMap<>();
        combined.put("status", unique.isEmpty() ? "NO_MATCH" : "SCHEDULED");
        combined.put("source", "BusTimetable.lk + TimeKeeper.lk");
        combined.put("sourceUrl", primary.getOrDefault("sourceUrl", "https://bustimetable.lk/makumbura-highway-bus-time-table/"));
        combined.put("fetchedAt", OffsetDateTime.now(SRI_LANKA).toString());
        combined.put("isLiveVehicleData", false);
        combined.put("notice", "Both sources publish schedules. They are scheduled timetables, not live GPS vehicle availability; verify before travel.");
        combined.put("sources", List.of(primary, timeKeeperPayload));
        combined.put("routes", new ArrayList<>(unique.values()));
        return combined;
    }

    private Set<String> discoverCandidatePages(String origin, String destination, String town) {
        Set<String> urls = new LinkedHashSet<>();
        urls.add(DEFAULT_URL);
        String location = slug(town);
        if (!location.isBlank()) urls.add(baseUrl + "/to/" + location + "/");
        if ("pottuvil".equalsIgnoreCase(safe(town)) || contains(origin, "pottuvil") || contains(destination, "pottuvil")) {
            urls.add(baseUrl + "/katunayaka-to-pottuvil-arugam-bay-highway-bus-timetable/");
            urls.add(baseUrl + "/pottuvil-arugam-bay-to-katunayaka-highway-bus-timetable/");
        }
        // Discover a more specific route page from the source index when possible.
        try {
            Document index = Jsoup.connect(DEFAULT_URL).userAgent("Mozilla/5.0 TourismService/1.0").timeout(10000).get();
            String needleA = safe(origin).toLowerCase(Locale.ROOT);
            String needleB = safe(destination).toLowerCase(Locale.ROOT);
            String needleTown = safe(town).toLowerCase(Locale.ROOT);
            for (Element a : index.select("a[href]")) {
                String label = a.text().toLowerCase(Locale.ROOT);
                String href = a.absUrl("href");
                if (href.isBlank() || !href.startsWith(baseUrl)) continue;
                boolean matches = (!needleA.isBlank() && label.contains(needleA))
                        || (!needleB.isBlank() && label.contains(needleB))
                        || (!needleTown.isBlank() && label.contains(needleTown));
                if (matches && (label.contains("timetable") || label.contains("schedule") || label.contains("bus"))) {
                    urls.add(href);
                }
            }
        } catch (Exception ignored) {
            // Keep the deterministic fallback URLs above.
        }
        return urls;
    }

    private List<Map<String, Object>> extractTables(Document doc, String url, String origin, String destination, String town) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Element table : doc.select("table")) {
            Elements trs = table.select("tr");
            if (trs.isEmpty()) continue;
            List<String> headers = cells(trs.get(0));
            for (int i = 1; i < trs.size(); i++) {
                List<String> cells = cells(trs.get(i));
                if (cells.isEmpty()) continue;
                String joined = String.join(" ", cells);
                if (!looksLikeTime(joined) && !looksLikeRoute(joined)) continue;
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("origin", firstNonBlank(origin, inferOrigin(doc.title(), town)));
                row.put("destination", firstNonBlank(destination, inferDestination(doc.title(), town)));
                row.put("departure", cells.size() > 0 ? cells.get(0) : "");
                row.put("arrivalOrRoute", cells.size() > 1 ? String.join(" | ", cells.subList(1, cells.size())) : "");
                row.put("busNumber", findBusNumber(joined));
                row.put("operator", findOperator(joined));
                row.put("sourceUrl", url);
                row.put("source", SOURCE_NAME);
                row.put("status", "SCHEDULED");
                rows.add(row);
            }
        }
        // Some pages use prose schedules rather than tables. Keep the page as a useful route record.
        if (rows.isEmpty()) {
            String text = doc.body().text();
            if (contains(text, "bus") && (contains(text, "ampara") || contains(text, "pottuvil") || contains(text, "matara"))) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("origin", firstNonBlank(origin, inferOrigin(doc.title(), town)));
                row.put("destination", firstNonBlank(destination, inferDestination(doc.title(), town)));
                row.put("departure", "See published page");
                row.put("arrivalOrRoute", summarizeSchedule(text));
                row.put("busNumber", findBusNumber(text));
                row.put("operator", findOperator(text));
                row.put("sourceUrl", url);
                row.put("source", SOURCE_NAME);
                row.put("status", "SCHEDULED");
                rows.add(row);
            }
        }
        return rows;
    }

    private List<String> cells(Element tr) {
        List<String> out = new ArrayList<>();
        for (Element cell : tr.select("th,td")) {
            String t = cell.text().replaceAll("\\s+", " ").trim();
            if (!t.isBlank()) out.add(t);
        }
        return out;
    }

    private boolean looksLikeTime(String s) { return s.matches(".*\\b\\d{1,2}(:\\d{2})?\\s*(am|pm|AM|PM)\\b.*"); }
    private boolean looksLikeRoute(String s) { return contains(s, "makumbura") || contains(s, "pottuvil") || contains(s, "ampara") || contains(s, "matara") || contains(s, "galle"); }
    private String findBusNumber(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(?:NB|NC|ND|NE|NG|NF)[-_]?[A-Z0-9]+\\b", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(s);
        return m.find() ? m.group() : "";
    }
    private String findOperator(String s) {
        String[] names = {"SLTB", "NCG Express", "Superline Travels", "DS Gunasekara", "LPN Highway Express", "Air Rose Highway Express", "Vishmitha Express", "Namali Travels"};
        for (String n : names) if (contains(s, n)) return n;
        return "";
    }
    private String summarizeSchedule(String text) {
        int idx = text.toLowerCase(Locale.ROOT).indexOf("schedule");
        if (idx < 0) idx = text.toLowerCase(Locale.ROOT).indexOf("departure");
        return idx >= 0 ? text.substring(idx, Math.min(text.length(), idx + 450)) : text.substring(0, Math.min(text.length(), 450));
    }
    private String inferOrigin(String title, String town) {
        String t = title == null ? "" : title.replaceAll("(?i)highway bus timetable|bus timetable", "").trim();
        String[] parts = t.split("\\s+to\\s+|\\s+-\\s+", 2);
        return parts.length > 0 && !parts[0].isBlank() ? parts[0].trim() : safe(town);
    }
    private String inferDestination(String title, String town) {
        String t = title == null ? "" : title.replaceAll("(?i)highway bus timetable|bus timetable", "").trim();
        String[] parts = t.split("\\s+to\\s+|\\s+-\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : safe(town);
    }
    private String slug(String s) { return safe(s).toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""); }
    private boolean contains(String a, String b) { return a != null && b != null && a.toLowerCase(Locale.ROOT).contains(b.toLowerCase(Locale.ROOT)); }
    private String safe(String s) { return s == null ? "" : s.trim(); }
    private String firstNonBlank(String a, String b) { return a != null && !a.isBlank() ? a : b; }

    private record CacheEntry(OffsetDateTime fetchedAt, Map<String, Object> payload) {}
}
