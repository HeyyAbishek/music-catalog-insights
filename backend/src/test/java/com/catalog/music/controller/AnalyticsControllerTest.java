package com.catalog.music.controller;

import com.catalog.music.model.AlbumItem;
import com.catalog.music.model.User;
import com.catalog.music.repository.AlbumItemRepository;
import com.catalog.music.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Static imports required for Mockito, MockMvc, and Hamcrest matchers
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlbumItemRepository albumItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AnalyticsController analyticsController;

    private User testUser;
    private List<AlbumItem> testAlbums;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        AlbumItem album1 = new AlbumItem();
        album1.setId(101L);
        album1.setTitle("OK Computer");
        album1.setArtistName("Radiohead");
        album1.setGenre("Alternative Rock");
        album1.setUserRating(5);
        album1.setReleaseDate(LocalDate.of(1997, 5, 21));
        album1.setUser(testUser);

        AlbumItem album2 = new AlbumItem();
        album2.setId(102L);
        album2.setTitle("Kid A");
        album2.setArtistName("Radiohead");
        album2.setGenre("Electronic");
        album2.setUserRating(4);
        album2.setReleaseDate(LocalDate.of(2000, 10, 2));
        album2.setUser(testUser);

        testAlbums = List.of(album1, album2);
    }

    @Test
    @DisplayName("GET /api/analytics - Returns correct calculated metrics")
    void getUserAnalytics_Success() throws Exception {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(albumItemRepository.findByUserId(1L)).thenReturn(testAlbums);

        mockMvc.perform(get("/api/analytics")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlbums", is(2)))
                .andExpect(jsonPath("$.averageRating", is(4.5)))
                .andExpect(jsonPath("$.genreDistribution['Alternative Rock']", is(1)))
                .andExpect(jsonPath("$.topArtists.Radiohead", is(2)));
    }

    @Test
    @DisplayName("GET /api/analytics - Handles empty library gracefully")
    void getUserAnalytics_EmptyLibrary() throws Exception {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(albumItemRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlbums", is(0)))
                .andExpect(jsonPath("$.averageRating", is(0.0)));
    }

    @Test
    @DisplayName("GET /api/analytics/ai-summary - Generates persona and AI insights")
    void getAiTrendSummary_Success() throws Exception {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(albumItemRepository.findByUserId(1L)).thenReturn(testAlbums);

        mockMvc.perform(get("/api/analytics/ai-summary")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persona", notNullValue()))
                .andExpect(jsonPath("$.summary", containsString("Analyzed 2 saved releases")))
                .andExpect(jsonPath("$.insights", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThan(0))));
    }
}