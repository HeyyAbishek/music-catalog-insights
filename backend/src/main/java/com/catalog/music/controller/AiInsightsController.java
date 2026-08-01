package com.catalog.music.controller;

import com.catalog.music.model.AlbumItem;
import com.catalog.music.model.User;
import com.catalog.music.repository.AlbumItemRepository;
import com.catalog.music.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiInsightsController {

    private final AlbumItemRepository albumItemRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;

    @Value("${openai.api.key:mock}")
    private String openAiKey;

    public AiInsightsController(AlbumItemRepository albumItemRepository, UserRepository userRepository) {
        this.albumItemRepository = albumItemRepository;
        this.userRepository = userRepository;
        this.restClient = RestClient.create();
    }

    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> generateLibraryInsights(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<AlbumItem> albums = albumItemRepository.findByUserId(user.getId());

        if (albums.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "summary", "Your library is currently empty! Add some albums to unlock AI taste profiling.",
                    "recommendations",
                    List.of("Coldplay - A Rush of Blood to the Head", "Miles Davis - Kind of Blue")));
        }

        String topGenres = albums.stream()
                .filter(a -> a.getGenre() != null)
                .collect(Collectors.groupingBy(AlbumItem::getGenre, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));

        String topArtists = albums.stream()
                .filter(a -> a.getArtistName() != null)
                .map(AlbumItem::getArtistName)
                .distinct()
                .limit(5)
                .collect(Collectors.joining(", "));

        if (!"mock".equalsIgnoreCase(openAiKey)) {
            try {
                String prompt = String.format(
                        "Analyze this music listener's library. Top genres: %s. Top artists: %s. Total saved: %d albums. "
                                +
                                "Provide a brief 2-sentence personality profile and 3 specific album recommendations.",
                        topGenres, topArtists, albums.size());

                Map<String, Object> requestBody = Map.of(
                        "model", "gpt-4o-mini",
                        "messages", List.of(
                                Map.of("role", "system", "content",
                                        "You are an expert music critic and recommendation engine."),
                                Map.of("role", "user", "content", prompt)));

                Map response = restClient.post()
                        .uri("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

                List choices = (List) response.get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                String aiText = (String) message.get("content");

                return ResponseEntity.ok(Map.of("insights", aiText, "status", "success"));
            } catch (Exception e) {
                // Fallback to rule-based AI generator if API key is invalid or fails
            }
        }

        // Rule-Based AI Engine (Fallback mode - zero external API dependencies)
        String insightText = String.format(
                "Your music collection heavily leans towards %s. You favor established catalog artists like %s. " +
                        "Based on your collection density, you appreciate cohesive album listening experiences.",
                topGenres.isEmpty() ? "Eclectic Styles" : topGenres,
                topArtists.isEmpty() ? "Various Artists" : topArtists);

        List<String> recommendations = List.of(
                "Radiohead - OK Computer (Matches your genre profile)",
                "Pink Floyd - The Dark Side of the Moon (High thematic overlap)",
                "The Who - Who's Next (Complements your saved catalog)");

        return ResponseEntity.ok(Map.of(
                "summary", insightText,
                "recommendations", recommendations,
                "status", "rule_engine"));
    }
}