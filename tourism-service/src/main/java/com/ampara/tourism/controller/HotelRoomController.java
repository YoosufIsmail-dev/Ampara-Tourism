package com.ampara.tourism.controller;

import com.ampara.tourism.entity.HotelRoom;
import com.ampara.tourism.repository.HotelRepository;
import com.ampara.tourism.repository.HotelRoomRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class HotelRoomController {
    private final HotelRoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    public HotelRoomController(HotelRoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository=roomRepository; this.hotelRepository=hotelRepository;
    }
    @GetMapping public List<HotelRoom> list(@RequestParam Long hotelId,
                                            @RequestParam(defaultValue="false") boolean availableOnly) {
        return availableOnly ? roomRepository.findByHotelIdAndAvailableTrue(hotelId) : roomRepository.findByHotelId(hotelId);
    }
    @GetMapping("/{id}") public ResponseEntity<HotelRoom> get(@PathVariable Long id) {
        return roomRepository.findById(id).map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.notFound().build());
    }
    @PostMapping public ResponseEntity<?> create(@RequestBody HotelRoom request) {
        if (request.getHotel()==null || request.getHotel().getId()==null)
            return ResponseEntity.badRequest().body("hotel.id is required");
        return hotelRepository.findById(request.getHotel().getId())
                .map(h -> { request.setHotel(h); return ResponseEntity.ok(roomRepository.save(request)); })
                .orElseGet(()->ResponseEntity.notFound().build());
    }
}