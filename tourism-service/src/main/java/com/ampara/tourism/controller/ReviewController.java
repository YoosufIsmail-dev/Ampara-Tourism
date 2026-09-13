package com.ampara.tourism.controller;

import com.ampara.tourism.dto.ReviewRequest;
import com.ampara.tourism.entity.Review;
import com.ampara.tourism.entity.User;
import com.ampara.tourism.repository.ReviewRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/place/{placeId}")
    public List<Review> byPlace(@PathVariable Long placeId) {
        return reviewRepository.findByPlaceId(placeId);
    }

    @GetMapping("/hotel/{hotelId}")
    public List<Review> byHotel(@PathVariable Long hotelId) {
        return reviewRepository.findByHotelId(hotelId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Review> create(@AuthenticationPrincipal User user, @Valid @RequestBody ReviewRequest request) {
        if (request.getPlaceId() == null && request.getHotelId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Review review = new Review(user, request.getPlaceId(), request.getHotelId(),
                request.getRating(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewRepository.save(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return reviewRepository.findById(id).map(review -> {
            boolean isOwner = review.getUser().getId().equals(user.getId());
            boolean isAdmin = user.getRole().name().equals("ADMIN");
            if (!isOwner && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            }
            reviewRepository.deleteById(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
