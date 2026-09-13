package com.ampara.tourism.controller;

import com.ampara.tourism.dto.HotelRequest;
import com.ampara.tourism.entity.Hotel;
import com.ampara.tourism.repository.HotelRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelRepository hotelRepository;

    public HotelController(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @GetMapping
    public List<Hotel> list(@RequestParam(required = false) String district,
                            @RequestParam(required = false) String town) {
        if (town != null) return hotelRepository.findByTownIgnoreCase(town);
        return district == null ? hotelRepository.findAll() : hotelRepository.findByDistrictIgnoreCase(district);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> get(@PathVariable Long id) {
        return hotelRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel create(@Valid @RequestBody HotelRequest request) {
        Hotel hotel = new Hotel(request.getName(), request.getDistrict(), request.getAddress(),
                request.getPricePerNight(), request.getRating(), request.getImageUrl(), request.getDescription());
        hotel.setTown(request.getTown());
        hotel.setLatitude(request.getLatitude());
        hotel.setLongitude(request.getLongitude());
        return hotelRepository.save(hotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hotel> update(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return hotelRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setDistrict(request.getDistrict());
                    existing.setTown(request.getTown());
                    existing.setAddress(request.getAddress());
                    existing.setLatitude(request.getLatitude());
                    existing.setLongitude(request.getLongitude());
                    existing.setPricePerNight(request.getPricePerNight());
                    existing.setRating(request.getRating());
                    existing.setImageUrl(request.getImageUrl());
                    existing.setDescription(request.getDescription());
                    return ResponseEntity.ok(hotelRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!hotelRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        hotelRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
