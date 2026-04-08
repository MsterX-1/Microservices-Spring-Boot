package com.moviecatalogservice.services;

import com.example.trendingmoviesservice.grpc.TrendingMoviesProto.TopMoviesRequest;
import com.example.trendingmoviesservice.grpc.TrendingMoviesProto.TopMoviesResponse;
import com.example.trendingmoviesservice.grpc.TrendingMoviesServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TrendingMoviesClientService {

    @GrpcClient("trending-movies-service")
    private TrendingMoviesServiceGrpc.TrendingMoviesServiceBlockingStub trendingMoviesStub;

    public TopMoviesResponse getTopMovies(int limit) {
        TopMoviesRequest request = TopMoviesRequest.newBuilder()
                .setLimit(limit)
                .build();
        return trendingMoviesStub.getTopMovies(request);
    }
}