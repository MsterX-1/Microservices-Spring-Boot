package com.example.trendingmoviesservice.gRPC;

import com.example.trendingmoviesservice.models.MovieRating;
import com.example.trendingmoviesservice.Services.TrendingCacheService;
import com.example.trendingmoviesservice.grpc.TrendingMoviesProto.MovieProto;
import com.example.trendingmoviesservice.grpc.TrendingMoviesProto.TopMoviesRequest;
import com.example.trendingmoviesservice.grpc.TrendingMoviesProto.TopMoviesResponse;
import com.example.trendingmoviesservice.grpc.TrendingMoviesServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class TrendingMoviesGrpcService
        extends TrendingMoviesServiceGrpc.TrendingMoviesServiceImplBase {

    @Autowired
    private TrendingCacheService trendingCacheService;

    @Override
    public void getTopMovies(TopMoviesRequest request,
                             StreamObserver<TopMoviesResponse> responseObserver) {
        int limit = request.getLimit();

        //  Check MongoDB cache first
        List<MovieRating> cachedMovies = trendingCacheService.getCachedTopMovies();

        if (cachedMovies == null || cachedMovies.isEmpty()) {
            System.out.println("Cache miss fetching top " + limit + " from ratings service");
            cachedMovies = trendingCacheService.fetchTrendingMovies(limit);
            // Save fetched data to cache
            trendingCacheService.saveToCache(cachedMovies);
        }


        List<MovieProto> topMovies = cachedMovies.stream()
                .limit(limit)
                .map(m -> MovieProto.newBuilder()
                        .setMovieId(m.getMovieId())
                        .setMovieName(m.getMovieName())
                        .setDescription(m.getDescription())
                        .setAverageRating(m.getAverageRating())
                        .build())
                .collect(Collectors.toList());

        TopMoviesResponse response = TopMoviesResponse.newBuilder()
                .addAllMovies(topMovies)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}