package com.catalog.music.service.impl;

import com.catalog.music.dto.AlbumRequestDto;
import com.catalog.music.dto.AlbumResponseDto;
import com.catalog.music.dto.ITunesSearchResponseDto;
import com.catalog.music.model.AlbumItem;
import com.catalog.music.model.User;
import com.catalog.music.repository.AlbumItemRepository;
import com.catalog.music.repository.UserRepository;
import com.catalog.music.service.AlbumItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlbumItemServiceImpl implements AlbumItemService {

    private final AlbumItemRepository albumItemRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;

    public AlbumItemServiceImpl(
            AlbumItemRepository albumItemRepository,
            UserRepository userRepository,
            RestClient restClient) {
        this.albumItemRepository = albumItemRepository;
        this.userRepository = userRepository;
        this.restClient = restClient;
    }

    @Override
    @Transactional(readOnly = true)
    public ITunesSearchResponseDto searchExternalAlbums(String query) {
        return restClient.get()
                .uri("https://itunes.apple.com/search?term={query}&entity=album", query)
                .retrieve()
                .body(ITunesSearchResponseDto.class);
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