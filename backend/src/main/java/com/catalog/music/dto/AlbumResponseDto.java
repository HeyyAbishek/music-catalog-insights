package com.catalog.music.dto;

import com.catalog.music.model.AlbumItem;
import java.time.Instant;
import java.time.LocalDate;

public class AlbumResponseDto {

    private Long id;
    private Long userId;
    private Long appleCatalogId;
    private String title;
    private String artistName;
    private String genre;
    private LocalDate releaseDate;
    private Integer trackCount;
    private String artworkUrl;
    private Integer userRating;
    private String userNotes;
    private Instant createdAt;
    private Instant updatedAt;

    public static AlbumResponseDto fromEntity(AlbumItem albumItem) {
        AlbumResponseDto dto = new AlbumResponseDto();
        dto.setId(albumItem.getId());
        dto.setUserId(albumItem.getUser() != null ? albumItem.getUser().getId() : null);
        dto.setAppleCatalogId(albumItem.getAppleCatalogId());
        dto.setTitle(albumItem.getTitle());
        dto.setArtistName(albumItem.getArtistName());
        dto.setGenre(albumItem.getGenre());
        dto.setReleaseDate(albumItem.getReleaseDate());
        dto.setTrackCount(albumItem.getTrackCount());
        dto.setArtworkUrl(albumItem.getArtworkUrl());
        dto.setUserRating(albumItem.getUserRating());
        dto.setUserNotes(albumItem.getUserNotes());
        dto.setCreatedAt(albumItem.getCreatedAt());
        dto.setUpdatedAt(albumItem.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAppleCatalogId() {
        return appleCatalogId;
    }

    public void setAppleCatalogId(Long appleCatalogId) {
        this.appleCatalogId = appleCatalogId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public Integer getUserRating() {
        return userRating;
    }

    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
