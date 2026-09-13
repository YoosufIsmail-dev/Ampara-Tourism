package com.ampara.tourism.controller;

import com.ampara.tourism.entity.FavoritePlace;
import com.ampara.tourism.entity.User;
import com.ampara.tourism.repository.FavoritePlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoritePlaceRepository favoriteRepository;

    public FavoriteController(FavoritePlaceRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping
    public List<FavoritePlace> myFavorites(@AuthenticationPrincipal User user) {
        return favoriteRepository.findByUser(user);
    }

    @PostMapping("/{placeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FavoritePlace> add(@AuthenticationPrincipal User user, @PathVariable Long placeId) {
        if (favoriteRepository.findByUserAndPlaceId(user, placeId).isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteRepository.save(new FavoritePlace(user, placeId)));
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal User user, @PathVariable Long placeId) {
        favoriteRepository.deleteByUserAndPlaceId(user, placeId);
        return ResponseEntity.noContent().build();
    }
}
