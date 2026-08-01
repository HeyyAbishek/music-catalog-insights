package com.catalog.music.controller;

import com.catalog.music.dto.AnalyticsDto;
import com.catalog.music.model.AlbumItem;
import com.catalog.music.model.User;
import com.catalog.music.repository.AlbumItemRepository;
import com.catalog.music.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AlbumItemRepository albumItemRepository;
    private final UserRepository userRepository;

    public AnalyticsController(AlbumItemRepository albumItemRepository, UserRepository userRepository) {
        this.albumItemRepository = albumItemRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/analytics
     * Returns structured dataset metrics for chart rendering (Genre, Ratings,
     * Release Years, Top Artists).
     * Cached under 'userAnalytics' key per username.
     */
    @GetMapping
    @Cacheable(value = "userAnalytics", key = "#authentication.name")
    public ResponseEntity<AnalyticsDto> getUserAnalytics(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<AlbumItem> albums = albumItemRepository.findByUserId(user.getId());

        if (albums.isEmpty()) {
            return ResponseEntity.ok(new AnalyticsDto(0, 0.0, Map.of(), Map.of(), Map.of(), Map.of()));
        }

        long totalAlbums = albums.size();

        double avgRating = albums.stream()
                .filter(a -> a.getUserRating() != null)
                .mapToInt(AlbumItem::getUserRating)
                .average()
                .orElse(0.0);

        Map<String, Long> genreDist = albums.stream()
                .filter(a -> a.getGenre() != null)
                .collect(Collectors.groupingBy(AlbumItem::getGenre, Collectors.counting()));

        Map<Integer, Long> ratingDist = albums.stream()
                .filter(a -> a.getUserRating() != null)
                .collect(Collectors.groupingBy(AlbumItem::getUserRating, Collectors.counting()));

        Map<Integer, Long> releaseYearDist = albums.stream()
                .filter(a -> a.getReleaseDate() != null)
                .collect(Collectors.groupingBy(a -> a.getReleaseDate().getYear(), TreeMap::new, Collectors.counting()));

        Map<String, Long> topArtists = albums.stream()
                .filter(a -> a.getArtistName() != null)
                .collect(Collectors.groupingBy(AlbumItem::getArtistName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return ResponseEntity.ok(new AnalyticsDto(
                totalAlbums,
                Math.round(avgRating * 10.0) / 10.0,
                genreDist,
                ratingDist,
                releaseYearDist,
                topArtists));
    }

    /**
     * GET /api/analytics/ai-summary
     * Generates natural language AI persona, insights, and recommendations based on
     * the user's saved library.
     * Cached under 'aiSummary' key per username.
     */
    @GetMapping("/ai-summary")
    @Cacheable(value = "aiSummary", key = "#authentication.name")
    public ResponseEntity<Map<String, Object>> getAiTrendSummary(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<AlbumItem> albums = albumItemRepository.findByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();

        if (albums.isEmpty()) {
            response.put("persona", "The Blank Slate");
            response.put("summary", "Your catalog is empty! Add a few albums to unlock AI insights.");
            response.put("insights", List.of("No data available yet. Start searching and saving tracks."));
            response.put("recommendations", List.of("Search for your favorite artist on the Search page."));
            return ResponseEntity.ok(response);
        }

        // 1. Calculate Top Genre
        Map<String, Long> genreCounts = albums.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getGenre() != null ? a.getGenre() : "Unknown",
                        Collectors.counting()));

        String topGenre = genreCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");

        // 2. Average Rating Calculation
        double avgRating = albums.stream()
                .mapToInt(a -> a.getUserRating() != null ? a.getUserRating() : 5)
                .average()
                .orElse(5.0);

        // 3. Archetype Persona Logic
        String persona;
        if (genreCounts.size() == 1) {
            persona = "The " + topGenre + " Purist";
        } else if (genreCounts.size() >= 4) {
            persona = "The Eclectic Tastemaker";
        } else if (avgRating >= 4.5) {
            persona = "The Enthusiastic Collector";
        } else {
            persona = "The Critical Curator";
        }

        // 4. Generate AI Bullet Insights
        List<String> insights = new ArrayList<>();
        insights.add(String.format("Your catalog strongly leans toward **%s**, making up %d%% of saved releases.",
                topGenre, (genreCounts.getOrDefault(topGenre, 0L) * 100) / albums.size()));

        if (avgRating >= 4.0) {
            insights.add(String.format(
                    "High satisfaction index: Your average rating across %d albums is a glowing %.1f / 5.0 stars.",
                    albums.size(), avgRating));
        } else {
            insights.add(String.format(
                    "Selective curation: Your average album rating is %.1f / 5.0 stars.", avgRating));
        }

        // 5. Smart Recommendations
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Explore sub-genres related to " + topGenre + " to expand your core collection.");
        recommendations.add("Try rating older releases in your library to balance out decade-level distribution.");

        response.put("persona", persona);
        response.put("summary", String.format(
                "Analyzed %d saved releases across %d distinct genres. Dominant style: %s.",
                albums.size(), genreCounts.size(), topGenre));
        response.put("insights", insights);
        response.put("recommendations", recommendations);

        return ResponseEntity.ok(response);
    }

    /**
     * Call this or trigger via AlbumController whenever a user adds/deletes/updates
     * an album
     * to purge out-of-date analytics caches.
     */
    @CacheEvict(value = { "userAnalytics", "aiSummary" }, key = "#authentication.name")
    public void clearUserAnalyticsCache(Authentication authentication) {
        // Evicts cache entries for this user
    }
}