package com.example.movieinfoservice.resources;

import com.example.movieinfoservice.models.Movie;
import com.example.movieinfoservice.models.MovieSummary;
import com.example.movieinfoservice.Repository.IMovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/movies")
public class MovieResource {

    @Value("${api.key}")
    private String apiKey;

    private RestTemplate restTemplate;

    @Autowired
    private IMovieRepository movieRepository;

    public MovieResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Real Scenario
    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId) {

        // Check MongoDB cache first
        Optional<Movie> cached = movieRepository.findById(movieId);
        if (cached.isPresent()) {
            System.out.println("RealMovieData: Cache HIT");
            return cached.get(); // cache hit
        }
        System.out.println("RealMovieData: Cache MISS");
        // Fetch from TMDB or generate fake data
        Movie movie = fetchMovie(movieId);

        // Save to MongoDB cache
        movieRepository.save(movie);

        return movie;
    }

    private Movie fetchMovie(String movieId) {
        try {
            final String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;
            MovieSummary movieSummary = restTemplate.getForObject(url, MovieSummary.class);
            return new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Movie not found for id: " + movieId
            );
        }
    }
    // Test With Cache
    @RequestMapping("/cache-test-fakeData")
    public Movie getFakeCachedData() {

        String movieId = "fakeData1";

        Optional<Movie> cached = movieRepository.findById(movieId);
        if (cached.isPresent()) {
            System.out.println("FakeData: Cache HIT");
            return cached.get(); // cache hit
        }
        System.out.println("FakeData: Cache MISS");

        simulateApiDelay();

        Movie movie = new Movie(
                movieId,
                "Fake Movie",
                "This is a cached fake movie used for load testing. ".repeat(50)
        );

        movieRepository.save(movie);

        return movie;
    }
    // Test Without Cache
    @RequestMapping("/fake-Mock-Data")
    public Movie getFakeMockData() {

        // Generate random movieId
        String movieId = String.valueOf((int)(Math.random() * 1000000));

        simulateApiDelay();

        // Generate fake movie
        Movie movie = new Movie(
                movieId,
                "Fake Movie " + movieId,
                "This is a fake movie description for testing performance. ".repeat(20)
        );


        return movie;
    }
    private void simulateApiDelay() {
        try {
            Thread.sleep(300); // 300ms simulated latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}