package com.ampara.tourism.controller;

import com.ampara.tourism.dto.VoiceGuideResponse;
import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.ampara.tourism.service.VoiceGuideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Voice Guide: returns a narration script (in en/ta/si) for a place, plus a link to
 * real synthesized MP3 audio (OpenAI TTS). Clients that would rather use on-device
 * text-to-speech (Web Speech API / mobile TTS) can read the script directly instead -
 * both are available side by side. See VoiceGuideService for the offline fallback
 * behaviour when no OPENAI_API_KEY is configured.
 */
@RestController
@RequestMapping("/api/voice-guide")
public class VoiceGuideController {

    private final TouristPlaceRepository placeRepository;
    private final VoiceGuideService voiceGuideService;

    public VoiceGuideController(TouristPlaceRepository placeRepository, VoiceGuideService voiceGuideService) {
        this.placeRepository = placeRepository;
        this.voiceGuideService = voiceGuideService;
    }

    @GetMapping("/places/{id}")
    public ResponseEntity<VoiceGuideResponse> forPlace(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "en") String lang) {
        TouristPlace place = placeRepository.findById(id).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }
        String script = voiceGuideService.scriptFor(place, lang);
        String ttsLocale = voiceGuideService.ttsLocaleFor(lang);
        String audioUrl = voiceGuideService.audioConfigured()
                ? "/api/voice-guide/places/" + id + "/audio?lang=" + lang
                : null;
        return ResponseEntity.ok(new VoiceGuideResponse(place.getId(), place.getName(), lang, script, ttsLocale, audioUrl));
    }

    /** Real synthesized MP3 narration via OpenAI TTS. 503 if OPENAI_API_KEY isn't configured or synthesis failed. */
    @GetMapping(value = "/places/{id}/audio", produces = "audio/mpeg")
    public ResponseEntity<byte[]> audioForPlace(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "en") String lang,
                                                 @RequestParam(defaultValue = "alloy") String voice) {
        TouristPlace place = placeRepository.findById(id).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] audio = voiceGuideService.audioFor(place, lang, voice);
        if (audio.length == 0) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header("Content-Disposition", "inline; filename=\"place-" + id + "-" + lang + ".mp3\"")
                .header("Cache-Control", "public, max-age=86400")
                .body(audio);
    }
}
