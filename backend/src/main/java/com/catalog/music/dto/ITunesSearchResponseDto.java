package com.catalog.music.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ITunesSearchResponseDto {

    private Integer resultCount;
    private List<ITunesAlbumResultDto> results = new ArrayList<>();

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public List<ITunesAlbumResultDto> getResults() {
        return results;
    }

    public void setResults(List<ITunesAlbumResultDto> results) {
        this.results = results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ITunesAlbumResultDto {

        @JsonProperty("collectionId")
        private Long appleCatalogId;

        @JsonProperty("collectionName")
        private String title;

        @JsonProperty("artistName")
        private String artistName;

        @JsonProperty("releaseDate")
        private String releaseDate;

        @JsonProperty("primaryGenreName")
        private String genre;

        @JsonProperty("artworkUrl100")
        private String artworkUrl;

        @JsonProperty("trackCount")
        private Integer trackCount;

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

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getGenre() {
            return genre;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public String getArtworkUrl() {
            return artworkUrl;
        }

        public void setArtworkUrl(String artworkUrl) {
            this.artworkUrl = artworkUrl;
        }

        public Integer getTrackCount() {
            return trackCount;
        }

        public void setTrackCount(Integer trackCount) {
            this.trackCount = trackCount;
        }
    }
}