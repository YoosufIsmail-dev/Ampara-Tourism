package com.ampara.tourism.service;

import com.ampara.tourism.entity.TouristPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Produces short spoken-style narration scripts for tourist places in the user's chosen
 * language, and can synthesize those scripts into real audio via the OpenAI TTS API
 * (GET /api/voice-guide/places/{id}/audio). Clients that would rather use on-device
 * text-to-speech (Web Speech API / mobile TTS) can keep using the plain-text script
 * endpoint instead - both are available.
 *
 * If the AI model isn't reachable (no OPENAI_API_KEY configured, network issue, etc.),
 * the script falls back to reading out the place's own stored description, and audio
 * synthesis is skipped (audio endpoint returns 503) so the rest of the app keeps working.
 */
@Service
public class VoiceGuideService {

    private static final Logger log = LoggerFactory.getLogger(VoiceGuideService.class);

    private static final Map<String, String> TTS_LOCALES = Map.of(
            "en", "en-US",
            "ta", "ta-IN",
            "si", "si-LK"
    );

    /** OpenAI TTS voices as of the "tts-1" / "tts-1-hd" models. */
    private static final Set<String> ALLOWED_VOICES = Set.of(
            "alloy", "echo", "fable", "onyx", "nova", "shimmer");
    private static final String DEFAULT_VOICE = "alloy";

    private final ChatClient chatClient;
    private final RestClient openAiRestClient;
    private final Map<String, String> scriptCache = new ConcurrentHashMap<>();
    private final Map<String, byte[]> audioCache = new ConcurrentHashMap<>();

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    public VoiceGuideService(ChatClient chatClient, RestClient openAiRestClient) {
        this.chatClient = chatClient;
        this.openAiRestClient = openAiRestClient;
    }

    public String ttsLocaleFor(String lang) {
        return TTS_LOCALES.getOrDefault(lang == null ? "en" : lang.toLowerCase(), "en-US");
    }

    public boolean audioConfigured() {
        return openAiApiKey != null && !openAiApiKey.isBlank();
    }

    public String normalizeVoice(String voice) {
        return (voice != null && ALLOWED_VOICES.contains(voice.toLowerCase())) ? voice.toLowerCase() : DEFAULT_VOICE;
    }

    /**
     * Synthesized MP3 audio for a place's narration, via OpenAI TTS. Returns an empty
     * array if no OPENAI_API_KEY is configured or synthesis fails - callers should treat
     * that as "audio unavailable" (e.g. respond 503) rather than serving a broken file.
     */
    public byte[] audioFor(TouristPlace place, String lang, String voice) {
        if (!audioConfigured()) {
            log.warn("OPENAI_API_KEY not set - cannot synthesize voice guide audio for place {}", place.getId());
            return new byte[0];
        }
        String normalizedVoice = normalizeVoice(voice);
        String script = scriptFor(place, lang);
        String cacheKey = place.getId() + ":" + (lang == null ? "en" : lang.toLowerCase()) + ":" + normalizedVoice;
        return audioCache.computeIfAbsent(cacheKey, k -> synthesize(script, normalizedVoice));
    }

    private byte[] synthesize(String text, String voice) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "tts-1",
                    "input", text,
                    "voice", voice,
                    "response_format", "mp3"
            );
            byte[] audio = openAiRestClient.post()
                    .uri("/audio/speech")
                    .body(body)
                    .retrieve()
                    .body(byte[].class);
            return audio != null ? audio : new byte[0];
        } catch (Exception e) {
            log.error("OpenAI TTS synthesis failed: {}", e.getMessage());
            return new byte[0];
        }
    }

    public String scriptFor(TouristPlace place, String lang) {
        String normalizedLang = (lang == null || lang.isBlank()) ? "en" : lang.toLowerCase();
        String cacheKey = place.getId() + ":" + normalizedLang;
        return scriptCache.computeIfAbsent(cacheKey, k -> generate(place, normalizedLang));
    }

    private String generate(TouristPlace place, String lang) {
        String languageName = switch (lang) {
            case "ta" -> "Tamil (தமிழ்)";
            case "si" -> "Sinhala (සිංහල)";
            default -> "English";
        };

        String knownDescription = place.getLocalizedDescription(lang);

        String prompt = """
                Write a warm, spoken-style audio-guide narration (2-3 short paragraphs, under 130 words)
                for tourists standing at "%s" (%s, %s, Sri Lanka), to be read aloud by text-to-speech.
                Write entirely in %s. Use simple, natural sentences suited for listening, not reading -
                avoid bullet points, headings, or markdown.

                Known facts about the place (only use these, don't invent new facts like prices,
                hours or history not given here):
                %s
                Category: %s
                Activities: %s
                """.formatted(
                place.getName(), place.getTown(), place.getDistrict(),
                languageName,
                (knownDescription == null || knownDescription.isBlank()) ? "(no description on file)" : knownDescription,
                place.getCategory(),
                place.getActivities() == null || place.getActivities().isEmpty() ? "(none listed)" : String.join(", ", place.getActivities())
        );

        try {
            String reply = chatClient.prompt().user(prompt).call().content();
            if (reply != null && !reply.isBlank()) {
                return reply.trim();
            }
        } catch (Exception e) {
            log.warn("Voice guide AI generation failed for place {} ({}), falling back to stored description: {}",
                    place.getId(), lang, e.getMessage());
        }

        // Fallback: just read out what we already have on file.
        String fallbackDescription = (knownDescription == null || knownDescription.isBlank())
                ? place.getLocalizedName(lang)
                : knownDescription;
        return place.getLocalizedName(lang) + ". " + fallbackDescription;
    }
}
