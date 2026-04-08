package com.example.trendingmoviesservice.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "trending_cache")
public class CachedTrendingMovies {

    @Id
    private String id = "trending_top10"; // fixed ID  only one document

    private List<MovieRating> movies;
    private LocalDateTime cachedAt;

    public CachedTrendingMovies() {}

    public CachedTrendingMovies(List<MovieRating> movies) {
        this.movies = movies;
        this.cachedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public List<MovieRating> getMovies() { return movies; }
    public void setMovies(List<MovieRating> movies) { this.movies = movies; }
    public LocalDateTime getCachedAt() { return cachedAt; }
    public void setCachedAt(LocalDateTime cachedAt) { this.cachedAt = cachedAt; }
}