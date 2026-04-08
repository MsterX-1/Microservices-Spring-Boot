package com.example.ratingsservice.models;

public class MovieAvgRating {

    private String movieId;
    private double averageRating;

    public MovieAvgRating() {}

    public MovieAvgRating(String movieId, double averageRating) {
        this.movieId = movieId;
        this.averageRating = averageRating;
    }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}