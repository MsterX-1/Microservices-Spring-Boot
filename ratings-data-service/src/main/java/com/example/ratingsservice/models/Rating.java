package com.example.ratingsservice.models;

import javax.persistence.*;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "movie_id")
    private String movieId;

    @Column(name = "rating")
    private int rating;

    public Rating() {}

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}