package com.ampara.tourism.controller;

import com.ampara.tourism.dto.TransportRouteRequest;
import com.ampara.tourism.entity.TransportRoute;
import com.ampara.tourism.repository.TransportRouteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
public class TransportController {

    private final TransportRouteRepository repository;

    public TransportController(TransportRouteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TransportRoute> list(@RequestParam(required = false) String type,
                                      @RequestParam(required = false) String origin,
                                      @RequestParam(required = false) String destination) {
        if (origin != null && destination != null) {
            return repository.findByOriginIgnoreCaseAndDestinationIgnoreCase(origin, destination);
        }
        if (origin != null || destination != null) {
            String place = origin != null ? origin : destination;
            return repository.findByOriginIgnoreCaseOrDestinationIgnoreCase(place, place);
        }
        if (type != null) {
            return repository.findByTypeIgnoreCase(type);
        }
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportRoute> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransportRoute create(@Valid @RequestBody TransportRouteRequest request) {
        return repository.save(fromRequest(new TransportRoute(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportRoute> update(@PathVariable Long id, @Valid @RequestBody TransportRouteRequest request) {
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

    private TransportRoute fromRequest(TransportRoute t, TransportRouteRequest r) {
        t.setType(r.getType());
        t.setRouteName(r.getRouteName());
        t.setOrigin(r.getOrigin());
        t.setDestination(r.getDestination());
        t.setDepartureTimes(r.getDepartureTimes());
        t.setDurationMinutesApprox(r.getDurationMinutesApprox());
        t.setFrequency(r.getFrequency());
        t.setFare(r.getFare());
        t.setOperatorName(r.getOperatorName());
        t.setNotes(r.getNotes());
        return t;
    }
}
