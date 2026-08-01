package com.catalog.music.service.impl;

import com.catalog.music.dto.AlbumRequestDto;
import com.catalog.music.dto.AlbumResponseDto;
import com.catalog.music.dto.ITunesSearchResponseDto;
import com.catalog.music.model.AlbumItem;
import com.catalog.music.model.User;
import com.catalog.music.repository.AlbumItemRepository;
import com.catalog.music.repository.UserRepository;
import com.catalog.music.service.AlbumItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlbumItemServiceImpl implements AlbumItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumItemServiceImpl.class);

    private final AlbumItemRepository albumItemRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;

    public AlbumItemServiceImpl(
            AlbumItemRepository albumItemRepository,
            UserRepository userRepository,
            RestClient.Builder restClientBuilder) {
        this.albumItemRepository = albumItemRepository;
        this.userRepository = userRepository;

        // Configure modern JDK HttpClient with automatic redirect follow & timeouts
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .baseUrl("https://itunes.apple.com")
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ITunesSearchResponseDto searchExternalAlbums(String query) {
        try {
            ITunesSearchResponseDto response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("term", query)
                            .queryParam("entity", "album")
                            .queryParam("limit", 25)
                            .build())
                    .retrieve()
                    .body(ITunesSearchResponseDto.class);

            if (response == null) {
                return new ITunesSearchResponseDto();
            }

            if (response.getResults() == null) {
                response.setResults(new ArrayList<>());
            }

            if (response.getResultCount() == null) {
                response.setResultCount(response.getResults().size());
            }

            return response;
        } catch (HttpMessageNotReadableException ex) {
            LOGGER.error("Received malformed iTunes response for query '{}': {}", query, ex.getMessage(), ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Received malformed album results from iTunes: " + ex.getMessage(),
                    ex);
        } catch (RestClientException ex) {
            LOGGER.error("Failed to fetch iTunes results for query '{}': {}", query, ex.getMessage(), ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to fetch album results from iTunes: " + ex.getMessage(),
                    ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error fetching iTunes results for query '{}': {}", query, ex.getMessage(), ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected search error: " + ex.getMessage(),
                    ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumResponseDto> getUserAlbums(String username) {
        User user = getUserByUsernameOrThrow(username);

        return albumItemRepository.findByUserId(user.getId())
                .stream()
                .map(AlbumResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponseDto getUserAlbum(Long id, String username) {
        User user = getUserByUsernameOrThrow(username);

        AlbumItem albumItem = albumItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Album not found with ID: " + id + " for user: " + username));

        return AlbumResponseDto.fromEntity(albumItem);
    }

    @Override
    public AlbumResponseDto addAlbum(AlbumRequestDto dto, String username) {
        User user = getUserByUsernameOrThrow(username);

        if (albumItemRepository.existsByUserIdAndAppleCatalogId(user.getId(), dto.getAppleCatalogId())) {
            throw new IllegalStateException(
                    "Album with Apple Catalog ID " + dto.getAppleCatalogId() + " is already in the user's catalog.");
        }

        AlbumItem albumItem = AlbumItem.builder()
                .user(user)
                .appleCatalogId(dto.getAppleCatalogId())
                .title(dto.getTitle())
                .artistName(dto.getArtistName())
                .genre(dto.getGenre())
                .releaseDate(dto.getReleaseDate())
                .trackCount(dto.getTrackCount())
                .artworkUrl(dto.getArtworkUrl())
                .userRating(dto.getUserRating())
                .userNotes(dto.getUserNotes())
                .build();

        AlbumItem savedItem = albumItemRepository.save(albumItem);
        return AlbumResponseDto.fromEntity(savedItem);
    }

    @Override
    public AlbumResponseDto updateAlbum(Long id, AlbumRequestDto dto, String username) {
        User user = getUserByUsernameOrThrow(username);

        AlbumItem albumItem = albumItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Album not found with ID: " + id + " for user: " + username));

        albumItem.setTitle(dto.getTitle());
        albumItem.setArtistName(dto.getArtistName());
        albumItem.setGenre(dto.getGenre());
        albumItem.setReleaseDate(dto.getReleaseDate());
        albumItem.setTrackCount(dto.getTrackCount());
        albumItem.setArtworkUrl(dto.getArtworkUrl());
        albumItem.setUserRating(dto.getUserRating());
        albumItem.setUserNotes(dto.getUserNotes());

        AlbumItem updatedItem = albumItemRepository.save(albumItem);
        return AlbumResponseDto.fromEntity(updatedItem);
    }

    @Override
    public void deleteAlbum(Long id, String username) {
        User user = getUserByUsernameOrThrow(username);

        AlbumItem albumItem = albumItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Album not found with ID: " + id + " for user: " + username));

        albumItemRepository.delete(albumItem);
    }

    private User getUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }
}