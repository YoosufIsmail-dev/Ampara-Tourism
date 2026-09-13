package com.ampara.tourism.controller;

import com.ampara.tourism.dto.EmergencyContactRequest;
import com.ampara.tourism.entity.EmergencyContact;
import com.ampara.tourism.repository.EmergencyContactRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {

    private final EmergencyContactRepository repository;

    public EmergencyContactController(EmergencyContactRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<EmergencyContact> list(@RequestParam(required = false) String category,
                                        @RequestParam(required = false) String district) {
        if (category != null) {
            return repository.findByCategoryIgnoreCase(category);
        }
        if (district != null) {
            return repository.findByDistrictIgnoreCaseOrNationwideTrue(district);
        }
        return repository.findAll();
    }

    @GetMapping("/nationwide")
    public List<EmergencyContact> nationwide() {
        return repository.findByNationwideTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContact> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmergencyContact create(@Valid @RequestBody EmergencyContactRequest request) {
        return repository.save(fromRequest(new EmergencyContact(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContact> update(@PathVariable Long id, @Valid @RequestBody EmergencyContactRequest request) {
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

    private EmergencyContact fromRequest(EmergencyContact c, EmergencyContactRequest r) {
        c.setName(r.getName());
        c.setCategory(r.getCategory());
        c.setPhoneNumber(r.getPhoneNumber());
        c.setDistrict(r.getDistrict());
        c.setTown(r.getTown());
        c.setAddress(r.getAddress());
        c.setNotes(r.getNotes());
        if (r.getNationwide() != null) c.setNationwide(r.getNationwide());
        return c;
    }
}
