package com.example.movieinfoservice.Repository;

import com.example.movieinfoservice.models.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IMovieRepository extends MongoRepository<Movie, String> {
}