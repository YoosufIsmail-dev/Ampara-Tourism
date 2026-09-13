package com.ampara.tourism.controller;

import com.ampara.tourism.dto.BookingRequest;
import com.ampara.tourism.entity.Booking;
import com.ampara.tourism.entity.BookingStatus;
import com.ampara.tourism.entity.Hotel;
import com.ampara.tourism.entity.User;
import com.ampara.tourism.repository.BookingRepository;
import com.ampara.tourism.repository.HotelRepository;
import com.ampara.tourism.repository.HotelRoomRepository;
import com.ampara.tourism.entity.HotelRoom;
import com.ampara.tourism.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final EmailService emailService;
    private final HotelRoomRepository roomRepository;

    public BookingController(BookingRepository bookingRepository, HotelRepository hotelRepository,
                              EmailService emailService, HotelRoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.emailService = emailService;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/me")
    public List<Booking> myBookings(@AuthenticationPrincipal User user) {
        return bookingRepository.findByUser(user);
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Booking> create(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody BookingRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElse(null);
        if (hotel == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            return ResponseEntity.badRequest().build();
        }

        HotelRoom room = null;
        double nightlyPrice = hotel.getPricePerNight() == null ? 0 : hotel.getPricePerNight();
        if (request.getRoomId() != null) {
            room = roomRepository.findByIdForUpdate(request.getRoomId()).orElse(null);
            if (room == null || room.getHotel() == null || !room.getHotel().getId().equals(hotel.getId()) ||
                    Boolean.FALSE.equals(room.getAvailable())) {
                return ResponseEntity.badRequest().build();
            }
            nightlyPrice = room.getPricePerNight() == null ? nightlyPrice : room.getPricePerNight();
            if (room.getCapacity() != null && request.getGuests() > room.getCapacity()) {
                return ResponseEntity.badRequest().build();
            }
            if (bookingRepository.existsOverlappingBooking(request.getRoomId(), request.getCheckIn(),
                    request.getCheckOut(), BookingStatus.CONFIRMED)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        double total = nightlyPrice * nights;

        Booking booking = new Booking(user, hotel, request.getCheckIn(), request.getCheckOut(),
                request.getGuests(), total);
        booking.setRoom(room);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingConfirmation(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return bookingRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .map(b -> {
                    b.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(b);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
