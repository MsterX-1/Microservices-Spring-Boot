package com.example.ratingsservice.resources;

import com.example.ratingsservice.Repository.IRatingRepository;
import com.example.ratingsservice.models.MovieAvgRating;
import com.example.ratingsservice.models.UserRating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ratings")
public class RatingsResource {

    @Autowired
    private IRatingRepository ratingRepository;

    @RequestMapping("/{userId}")
    public UserRating getRatingsOfUser(@PathVariable String userId) {
        return new UserRating(ratingRepository.findByUserId(userId));
    }
    // top movies by average rating
    @RequestMapping("/top/{limit}")
    public List<MovieAvgRating> getTopMoviesByRating(@PathVariable int limit) {
        List<Object[]> results = ratingRepository.findTopMoviesByAverageRating(
                PageRequest.of(0, limit)
        );
        return results.stream()
                .map(row -> new MovieAvgRating(
                        (String) row[0],
                        (Double) row[1]
                ))
                .collect(Collectors.toList());
    }
}