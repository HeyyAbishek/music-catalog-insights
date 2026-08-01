package com.catalog.music.dto;

import com.catalog.music.model.AlbumItem;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumResponse {

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

    public static AlbumResponse fromEntity(AlbumItem albumItem) {
        return AlbumResponse.builder()
                .id(albumItem.getId())
                .userId(albumItem.getUser() != null ? albumItem.getUser().getId() : null)
                .appleCatalogId(albumItem.getAppleCatalogId())
                .title(albumItem.getTitle())
                .artistName(albumItem.getArtistName())
                .genre(albumItem.getGenre())
                .releaseDate(albumItem.getReleaseDate())
                .trackCount(albumItem.getTrackCount())
                .artworkUrl(albumItem.getArtworkUrl())
                .userRating(albumItem.getUserRating())
                .userNotes(albumItem.getUserNotes())
                .createdAt(albumItem.getCreatedAt())
                .updatedAt(albumItem.getUpdatedAt())
                .build();
    }
}
