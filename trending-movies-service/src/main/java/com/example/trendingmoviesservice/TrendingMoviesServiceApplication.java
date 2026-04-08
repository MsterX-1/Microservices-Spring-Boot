package com.example.trendingmoviesservice;

import com.example.trendingmoviesservice.Services.TrendingCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
@EnableScheduling
public class TrendingMoviesServiceApplication {

    @Autowired
    private TrendingCacheService trendingCacheService;

    private final int TIMEOUT = 3000;   // 3 seconds

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(TIMEOUT);   // Set the timeout to 3 seconds

        return new RestTemplate(clientHttpRequestFactory);
    }

    // Initialize cache on startup
    @Bean
    public CommandLineRunner initCache(TrendingCacheService cacheService) {
        return args -> {
            try {
                System.out.println("Waiting before initializing cache...");
                Thread.sleep(30000); //  wait 30 seconds for Eureka + services

                cacheService.initializeCache();
                System.out.println("Cache initialized successfully.");

            } catch (Exception e) {
                System.out.println("Failed to initialize cache on startup: "
                        + e.getMessage()
                        + ". Cache will be initialized later.");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(TrendingMoviesServiceApplication.class, args);
    }
}