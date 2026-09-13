package com.ampara.tourism.controller;

import com.ampara.tourism.dto.ChatRequest;
import com.ampara.tourism.dto.ChatResponse;
import com.ampara.tourism.entity.Attraction;
import com.ampara.tourism.repository.AttractionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class ItineraryController {

    private final ChatClient chatClient;
    private final AttractionRepository attractionRepository;

    public ItineraryController(ChatClient chatClient, AttractionRepository attractionRepository) {
        this.chatClient = chatClient;
        this.attractionRepository = attractionRepository;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return new ChatResponse("Please tell me what kind of trip you're planning.");
        }

        List<Attraction> attractions = (request.district() == null || request.district().isBlank())
                ? attractionRepository.findAll()
                : attractionRepository.findByDistrictIgnoreCase(request.district());

        String context = buildContext(attractions);

        String userPrompt = """
                Available attractions:
                %s

                Traveler request: %s
                """.formatted(context, request.message());

        String reply = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        return new ChatResponse(reply);
    }

    private String buildContext(List<Attraction> attractions) {
        if (attractions.isEmpty()) {
            return "(no attractions found in the database)";
        }
        return attractions.stream()
                .map(a -> "- %s (%s, %s): %s | Best season: %s | Suggested duration: %s day(s) | Activities: %s".formatted(
                        a.getName(),
                        a.getDistrict(),
                        a.getCategory(),
                        a.getDescription(),
                        a.getBestSeason(),
                        a.getSuggestedDurationDays(),
                        String.join(", ", a.getActivities())
                ))
                .collect(Collectors.joining("\n"));
    }
}
