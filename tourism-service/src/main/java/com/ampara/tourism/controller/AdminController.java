package com.ampara.tourism.controller;

import com.ampara.tourism.dto.AdminStatsResponse;
import com.ampara.tourism.entity.Booking;
import com.ampara.tourism.entity.BookingStatus;
import com.ampara.tourism.entity.User;
import com.ampara.tourism.repository.AttractionRepository;
import com.ampara.tourism.repository.BookingRepository;
import com.ampara.tourism.repository.HotelRepository;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.ampara.tourism.repository.ReviewRepository;
import com.ampara.tourism.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AttractionRepository attractionRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TouristPlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;

    public AdminController(AttractionRepository attractionRepository, HotelRepository hotelRepository,
                            BookingRepository bookingRepository, UserRepository userRepository,
                            TouristPlaceRepository placeRepository, ReviewRepository reviewRepository) {
        this.attractionRepository = attractionRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                attractionRepository.count(),
                hotelRepository.count(),
                bookingRepository.count(),
                userRepository.count(),
                placeRepository.count(),
                reviewRepository.count()
        );
    }

    @GetMapping("/bookings")
    public List<Booking> allBookings() {
        return bookingRepository.findAll();
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        return bookingRepository.findById(id)
                .map(b -> {
                    b.setStatus(status);
                    return ResponseEntity.ok(bookingRepository.save(b));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public List<User> allUsers() {
        return userRepository.findAll();
    }
}
