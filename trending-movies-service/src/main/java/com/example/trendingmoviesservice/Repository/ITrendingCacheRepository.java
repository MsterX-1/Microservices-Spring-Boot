package com.example.trendingmoviesservice.Repository;

import com.example.trendingmoviesservice.models.CachedTrendingMovies;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ITrendingCacheRepository
        extends MongoRepository<CachedTrendingMovies, String> {
}