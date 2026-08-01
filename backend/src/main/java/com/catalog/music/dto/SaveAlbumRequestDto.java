package com.catalog.music.dto;

import lombok.Data;

@Data
public class SaveAlbumRequestDto {
    private Long collectionId;
    private String collectionName;
    private String artistName;
    private String releaseDate;
    private String primaryGenreName;
    private String artworkUrl100;
    private Integer trackCount;
    private Long userId; // ID of the user adding the album to their catalog
}