package com.catalog.music.dto;

import java.util.Map;

public class AnalyticsDto {
    private long totalAlbums;
    private double averageRating;
    private Map<String, Long> genreDistribution; // For Pie/Donut Chart
    private Map<Integer, Long> ratingDistribution; // For Histogram/Bar Chart
    private Map<Integer, Long> releasesByYear; // For Line/Area Chart
    private Map<String, Long> topArtists; // For Horizontal Bar Chart

    public AnalyticsDto() {
    }

    public AnalyticsDto(long totalAlbums, double averageRating, Map<String, Long> genreDistribution,
            Map<Integer, Long> ratingDistribution, Map<Integer, Long> releasesByYear,
            Map<String, Long> topArtists) {
        this.totalAlbums = totalAlbums;
        this.averageRating = averageRating;
        this.genreDistribution = genreDistribution;
        this.ratingDistribution = ratingDistribution;
        this.releasesByYear = releasesByYear;
        this.topArtists = topArtists;
    }

    public long getTotalAlbums() {
        return totalAlbums;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public Map<String, Long> getGenreDistribution() {
        return genreDistribution;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public Map<Integer, Long> getReleasesByYear() {
        return releasesByYear;
    }

    public Map<String, Long> getTopArtists() {
        return topArtists;
    }
}