package com.example.trendingmoviesservice.models;

public class MovieRating {

    private String movieId;
    private String movieName;
    private String description;
    private double averageRating;

    public MovieRating() {}

    public MovieRating(String movieId, double averageRating) {
        this.movieId = movieId;
        this.averageRating = averageRating;
    }

    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}