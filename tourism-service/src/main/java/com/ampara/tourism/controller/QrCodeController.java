package com.ampara.tourism.controller;

import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.ampara.tourism.service.QrCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generates a QR code for a tourist place that links to its detail page/deep link.
 * Meant to be printed on signage at the physical site so visitors can scan it for
 * info in their preferred language.
 */
@RestController
@RequestMapping("/api/places")
public class QrCodeController {

    private final TouristPlaceRepository placeRepository;
    private final QrCodeService qrCodeService;

    @Value("${app.public-base-url:https://ampara-tourism.example}")
    private String publicBaseUrl;

    public QrCodeController(TouristPlaceRepository placeRepository, QrCodeService qrCodeService) {
        this.placeRepository = placeRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * PNG QR code for a place. Scanning it opens {publicBaseUrl}/places/{id}?lang={lang}.
     * size: pixel width/height of the square image (default 300).
     */
    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id,
                                         @RequestParam(defaultValue = "300") int size,
                                         @RequestParam(defaultValue = "en") String lang) {
        TouristPlace place = placeRepository.findById(id).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }
        String targetUrl = publicBaseUrl + "/places/" + id + "?lang=" + lang;
        try {
            byte[] png = qrCodeService.generatePng(targetUrl, size);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"place-" + id + "-qr.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(png);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Just the target URL the QR code encodes, useful for debugging/printing captions. */
    @GetMapping("/{id}/qrcode/target-url")
    public ResponseEntity<String> targetUrl(@PathVariable Long id, @RequestParam(defaultValue = "en") String lang) {
        if (!placeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(publicBaseUrl + "/places/" + id + "?lang=" + lang);
    }
}
