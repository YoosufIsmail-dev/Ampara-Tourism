package com.ampara.tourism.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Plain REST client for OpenAI endpoints not covered by Spring AI's ChatClient
 * (currently: text-to-speech, used by VoiceGuideService). Reuses the same
 * OPENAI_API_KEY as the AI trip planner / voice guide script generation.
 */
@Configuration
public class OpenAiRestClientConfig {

    @Bean
    public RestClient openAiRestClient(@Value("${spring.ai.openai.api-key:}") String apiKey) {
        return RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
}
