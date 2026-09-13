package com.ampara.tourism.controller;

import com.ampara.tourism.dto.EventRequest;
import com.ampara.tourism.entity.Event;
import com.ampara.tourism.repository.EventRepository;
import com.ampara.tourism.service.PushNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final DateTimeFormatter PUSH_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    private final EventRepository repository;
    private final PushNotificationService pushNotificationService;

    public EventController(EventRepository repository, PushNotificationService pushNotificationService) {
        this.repository = repository;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping
    public List<Event> list(@RequestParam(required = false) String category,
                             @RequestParam(required = false) String town,
                             @RequestParam(required = false) Long placeId) {
        if (category != null) {
            return repository.findByCategoryIgnoreCaseOrderByStartDateTimeAsc(category);
        }
        if (town != null) {
            return repository.findByTownIgnoreCaseOrderByStartDateTimeAsc(town);
        }
        if (placeId != null) {
            return repository.findByPlaceIdOrderByStartDateTimeAsc(placeId);
        }
        return repository.findAll();
    }

    /** Events starting from now onward, soonest first. */
    @GetMapping("/upcoming")
    public List<Event> upcoming() {
        return repository.findByStartDateTimeAfterOrderByStartDateTimeAsc(LocalDateTime.now());
    }

    @GetMapping("/range")
    public List<Event> range(@RequestParam String start, @RequestParam String end) {
        return repository.findByStartDateTimeBetweenOrderByStartDateTimeAsc(
                LocalDateTime.parse(start), LocalDateTime.parse(end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@Valid @RequestBody EventRequest request) {
        Event saved = repository.save(fromRequest(new Event(), request));
        notifySubscribers(saved);
        return saved;
    }

    /** Best-effort: never let a push failure block event creation. */
    private void notifySubscribers(Event event) {
        try {
            String when = event.getStartDateTime() != null ? event.getStartDateTime().format(PUSH_DATE_FORMAT) : "";
            String body = event.getTown() != null ? when + " · " + event.getTown() : when;
            Map<String, String> data = new HashMap<>();
            data.put("type", "event");
            data.put("eventId", String.valueOf(event.getId()));
            pushNotificationService.sendToTopic(PushNotificationService.EVENTS_TOPIC,
                    "New event: " + event.getTitle(), body, data);
        } catch (Exception ignored) {
            // Push is a nice-to-have here; event creation itself already succeeded.
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return repository.findById(id)
                .map(existing -> ResponseEntity.ok(repository.save(fromRequest(existing, request))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Event fromRequest(Event e, EventRequest r) {
        e.setTitle(r.getTitle());
        e.setTitleTa(r.getTitleTa());
        e.setTitleSi(r.getTitleSi());
        e.setDescription(r.getDescription());
        e.setCategory(r.getCategory());
        e.setStartDateTime(r.getStartDateTime());
        e.setEndDateTime(r.getEndDateTime());
        e.setLocation(r.getLocation());
        e.setTown(r.getTown());
        e.setPlaceId(r.getPlaceId());
        if (r.getRecurringYearly() != null) e.setRecurringYearly(r.getRecurringYearly());
        return e;
    }
}
