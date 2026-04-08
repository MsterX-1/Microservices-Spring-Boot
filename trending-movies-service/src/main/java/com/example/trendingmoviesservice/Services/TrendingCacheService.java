package com.example.trendingmoviesservice.Services;

import com.example.trendingmoviesservice.Repository.ITrendingCacheRepository;
import com.example.trendingmoviesservice.models.CachedTrendingMovies;
import com.example.trendingmoviesservice.models.MovieRating;
import com.example.trendingmoviesservice.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class TrendingCacheService {

    @Autowired
    private ITrendingCacheRepository trendingCacheRepository;
    private final RestTemplate restTemplate;
    public TrendingCacheService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }



    // Runs once per day
    @Scheduled(initialDelay = 86400000, fixedRate = 86400000)
    public void refreshTrendingCache() {
        System.out.println("Scheduled (daily): Refreshing trending cache from ratings service...");

        List<MovieRating> topMovies = fetchTrendingMovies(10);

        for (MovieRating movie : topMovies) {
            try {
                String movieDetailsUrl = "http://movie-info-service/movies/" + movie.getMovieId();

                Movie movieDetails = restTemplate.getForObject(movieDetailsUrl, Movie.class);

                // Copy data into your cached object
                if (movieDetails != null) {
                    movie.setMovieName(movieDetails.getName());
                    movie.setDescription(movieDetails.getDescription());
                } else {
                    movie.setMovieName("Unknown");
                    movie.setDescription("No description available");
                }

            } catch (Exception e) {
                e.printStackTrace();
                movie.setMovieName("Error");
                movie.setDescription("Failed to fetch movie info");
            }
        }

        // Save to MongoDB
        if (topMovies != null && !topMovies.isEmpty()) {
            CachedTrendingMovies cache = new CachedTrendingMovies(topMovies);
            trendingCacheRepository.save(cache);
            System.out.println("Scheduled (daily): Trending cache (saved) refreshed at: " + cache.getCachedAt());
        }
    }

    public List<MovieRating> fetchTrendingMovies(int limit) {
        System.out.println("Fetching top " + limit + " movies from ratings service...");

        String url = "http://localhost:8083/ratings/top/" + limit;
        RestTemplate plainRestTemplate = new RestTemplate();
        return plainRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MovieRating>>() {}
        ).getBody();
    }

    public List<MovieRating> getCachedTopMovies() {
        Optional<CachedTrendingMovies> cached = trendingCacheRepository
                .findById("trending_top10");

        if (cached.isPresent()) {
            System.out.println("Serving from MongoDB cache (cached at: "
                    + cached.get().getCachedAt() + ")");
            return cached.get().getMovies();
        }
        return null; // cache not ready
    }

    public void saveToCache(List<MovieRating> movies) {
        CachedTrendingMovies cache = new CachedTrendingMovies(movies);
        trendingCacheRepository.save(cache);
        System.out.println("Cache saved at: " + cache.getCachedAt());
    }

    // Call this manually to populate cache immediately on startup
    public void initializeCache() {
        if (trendingCacheRepository.findById("trending_top10").isEmpty()) {
            System.out.println("Cache empty — initializing...");
            refreshTrendingCache();
        }
    }
}