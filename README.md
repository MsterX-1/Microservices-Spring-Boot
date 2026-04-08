# Spring Boot Microservices - Movie Rating Application

A microservices architecture for a movie rating system with service discovery, caching, and inter-service communication.

## Services

- **Discovery Server** (Port 8761): Eureka service registry
- **Movie Catalog Service** (Port 8081): Aggregates user catalogs from ratings and movie info
- **Movie Info Service** (Port 8082): Provides movie details, caches in MongoDB
- **Ratings Data Service** (Port 8083): Manages user ratings in MySQL
- **Trending Movies Service** (Ports 8084/gRPC 9090): Calculates trending movies, caches full data in MongoDB

## Endpoints

### Movie Catalog Service (Port 8081)

- **GET /catalog/{userId}**: Returns user's movie catalog with ratings and movie details
- **GET /catalog/trending**: Returns top trending movies with full details

### Movie Info Service (Port 8082)

- **GET /movies/{movieId}**: Returns movie details (name, description, etc.)
- **GET /movies/cache-test-fakeData**: Test endpoint with caching (returns fake data)
- **GET /movies/fake-Mock-Data**: Test endpoint without caching (returns fake data)

### Ratings Data Service (Port 8083)

- **GET /ratings/{userId}**: Returns user's ratings
- **GET /ratings/top/{limit}**: Returns top movies by average rating

### Trending Movies Service (Port 8084 / gRPC 9090)

- **gRPC GetTopMovies(limit)**: Returns top trending movies with full details via protobuf
- **REST GET /trending** (if available): Alternative REST endpoint for trending movies

### Discovery Server (Port 8761)

- Eureka registration and discovery endpoints (standard Spring Cloud Eureka)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway / Client                    │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┬──────────────┐
        │            │            │              │
        ▼            ▼            ▼              ▼
   ┌─────────┐  ┌──────────┐ ┌──────────┐ ┌──────────────┐
   │Discovery│  │  Movie   │ │ Ratings  │ │  Trending    │
   │ Server  │  │  Catalog │ │   Data   │ │   Movies     │
   │(Port    │  │  Service │ │ Service  │ │  Service     │
   │ 8761)   │  │(Port 8081)│(Port 8083)│ │(Port 8084)   │
   └─────────┘  └───┬──────┘ └─────┬────┘ │gRPC:9090     │
        ▲            │             │      └──────────────┘
        │    ┌───────┼─────────────┼──────┐
        │    │       │             │      │
        └────┴───────┴─────────────┴──────┘
            (Service Discovery via Eureka)

        ┌───────────┐    ┌────────────┐    ┌─────────────┐
        │  TheMovieDB   │   MongoDB     │   │  MySQL       │
        │   (External API)  │(Movie Cache)  (Ratings DB)
        └───────────┘    └────────────┘    └─────────────┘
```

## Key Data Flows

### User Catalog (GET /catalog/{userId})

Movie Catalog Service → Ratings Data Service (REST) → MySQL  
Movie Catalog Service → Movie Info Service (REST) → MongoDB Cache / TheMovieDB API  
Returns: Movie details + user ratings

### Trending Movies (GET /catalog/trending)

Movie Catalog Service → Trending Movies Service (gRPC)  
Trending Service checks MongoDB cache; on miss, fetches from Ratings Data Service (REST) + enriches with Movie Info Service (REST), caches full data  
Returns: Top movies with full details via protobuf

## Complete System Data Flow

```
┌────────────────────────────────────────────────────────────────────────────┐
│                     COMPLETE MICROSERVICES DATA FLOW                         │
└────────────────────────────────────────────────────────────────────────────┘

                                  ┏━━━━━━━━━━━┓
                                  ┃ CLIENT APP ┃
                                  ┗━━━━┬━━━━━━┛
                                       │
                ┌──────────────────────┼──────────────────────┐
                │                      │                      │
                │                      │                      │
    ┌───────────▼──────────┐  ┌────────▼────────┐  ┌────────▼────────┐
    │ (1) Request User     │  │ (2) Request     │  │ (3) Request     │
    │     Catalog          │  │ Trending Movies │  │ Movie Info      │
    │ GET /catalog/{uid}   │  │ GET /trending   │  │ GET /movies/{id}│
    │                      │  │                 │  │                 │
    └───────────┬──────────┘  └────────┬────────┘  └────────┬────────┘
                │                      │                    │
                │ REST/HTTP            │ REST/HTTP         │ REST/HTTP
                │ (localhost:8081)     │ (localhost:8084)  │ (localhost:8082)
                │                      │                   │
    ┌───────────▼──────────────────────▼───────────────────▼─────┐
    │           MOVIE CATALOG SERVICE (Port 8081)                 │
    │  ┌─────────────────────────────────────────────────────┐   │
    │  │ • Aggregates data from multiple services            │   │
    │  │ • Combines ratings + movie info + trending          │   │
    │  │ • Registered with Eureka (Service Discovery)        │   │
    │  └─────────────────────────────────────────────────────┘   │
    └───────────┬──────────────────┬──────────────────┬──────────┘
                │                  │                  │
    ┌───────────▼──────┐  ┌────────▼─────────┐  ┌────▼──────────┐
    │ REST/HTTP to     │  │ REST/HTTP to     │  │ gRPC to       │
    │ Movie Info (8082)│  │ Ratings Data     │  │ Trending      │
    │ localhost:8081   │  │ (8083)           │  │ (gRPC:9090)   │
    └───────────┬──────┘  │ localhost:8083   │  └────┬──────────┘
                │         └────────┬─────────┘       │
    ┌───────────▼──────────────────▼───────────────────▼─────┐
    │                    MICROSERVICES LAYER                  │
    │                                                         │
    │  ┌─────────────────────────┐    ┌──────────────────┐  │
    │  │ MOVIE INFO SERVICE      │    │ RATINGS DATA     │  │
    │  │ Port: 8082              │    │ SERVICE          │  │
    │  │ • REST/HTTP             │    │ Port: 8083       │  │
    │  │ • Calls TheMovieDB API  │    │ • REST/HTTP      │  │
    │  │ • Caches in MongoDB     │    │ • Queries MySQL  │  │
    │  │ • Cache HIT/MISS logic  │    │ • User ratings   │  │
    │  └────────┬────────────────┘    └──────────┬───────┘  │
    │           │                                │           │
    │  ┌────────▼──────────────────────────────▼──────────┐  │
    │  │ TRENDING MOVIES SERVICE (Ports: 8084 / gRPC 9090) │  │
    │  │ • REST/HTTP & gRPC protocols                      │  │
    │  │ • Startup delay (30s) for service registration    │  │
    │  │ • Caches full movie data in MongoDB               │  │
    │  │ • Scheduled daily refresh (24h)                   │  │
    │  │ • On cache miss: fetches ratings + enriches with  │  │
    │  │   movie info, caches full data                     │  │
    │  └────────────────────┬─────────────────────────────┘  │
    │                       │                                 │
    └────────────┬──────────┼──────────────────────┬──────────┘
                 │          │                      │
    ┌────────────▼──────────▼──────────────────────▼──────┐
    │           DATABASE & EXTERNAL API LAYER             │
    │                                                     │
    │  ┌──────────────┐    ┌──────────────┐    ┌──────┐  │
    │  │  MYSQL DB    │    │ MONGODB      │    │ API  │  │
    │  │ ratingsdb    │    │ moviecache   │    │Theme │  │
    │  │ localhost:   │    │ localhost:   │    │ Movie│  │
    │  │ 3306         │    │ 27017        │    │ DB   │  │
    │  │              │    │              │    │      │  │
    │  │ • user_rating│    │ • movies     │    │ REST │  │
    │  │   table      │    │ • trending_  │    │ API  │  │
    │  │ • rating     │    │   movies     │    │      │  │
    │  │   table      │    │              │    │      │  │
    │  └──────────────┘    └──────────────┘    └──────┘  │
    │                                                     │
    └─────────────────────────────────────────────────────┘

KEY DATA FLOWS:
═════════════════════════════════════════════════════════════════════════════

A. User Catalog Request (GET /catalog/{userId})
   Client
     ↓
   Movie Catalog Service (REST 8081)
     ├─→ Ratings Data Service (REST 8083) [GET /ratings/{userId}]
     │    └─→ MySQL ratingsdb [SELECT user ratings]
     │        └─→ Returns: [{movieId, rating}, ...]
     └─→ Movie Info Service (REST 8082) [GET /movies/{movieId}] × N
          ├─→ Check MongoDB Cache (HIT/MISS)
          │   └─→ If MISS: Fetch from TheMovieDB API
          │        └─→ SAVE to MongoDB Cache
          └─→ Returns: [{name, description, genre, ...}, ...]

   Result: Combined catalog with movie details + user ratings

B. Trending Movies Request (GET /catalog/trending) [UPDATED]
   Client
     ↓
   Movie Catalog Service (REST 8081)
     └─→ Trending Movies Service (gRPC 9090) [GetTrendingMovies]
          ├─→ Check MongoDB Cache [cached_trending_movies]
          │   └─→ If HIT: Return cached full movie data immediately
          │   └─→ If MISS:
          │        ├─→ Fetch from MySQL [SELECT TOP N by avg rating]
          │        ├─→ For each movie: Fetch details from Movie Info Service (REST)
          │        └─→ SAVE full data to MongoDB Cache
          └─→ Returns: [{movieId, name, description, avgRating}, ...] via protobuf

   Result: Top trending movies with full details (no additional calls needed)

C. Movie Info Service Caching (GET /movies/{movieId})
   Movie Info Service (8082 / REST)
     ├─→ Check MongoDB Cache
     │   ├─→ If HIT: Return cached movie data immediately
     │   └─→ If MISS:
     │        └─→ Call TheMovieDB API [external-network]
     │             └─→ Returns: Full movie data
     │             └─→ SAVE to MongoDB Cache
     └─→ Return to Requester

   Result: Movie details with fast cache-hit performance

D. Ratings Data Service Query (GET /ratings/{userId})
   Ratings Data Service (8083 / REST)
     └─→ Query MySQL ratingsdb [No caching - direct DB access]
         └─→ SELECT * FROM user_rating WHERE userId = ?
             └─→ Returns: User's complete rating history

   Result: Real-time rating data with ACID guarantees

E. Trending Movies Service Caching Strategy [UPDATED]
   Startup Phase:
     Trending Movies Service starts
       ├─→ Wait 30 seconds for Eureka service registration
       └─→ Try to load from MySQL [SELECT TOP 10 by avg rating]
            ├─→ Enrich with movie details from Movie Info Service
            │   └─→ If FAIL (services down): Log warning, continue startup anyway
            └─→ SAVE full data to MongoDB Cache for startup completion

   First Request Phase (if startup failed):
     Client request [GET /trending]
       └─→ gRPC GetTrendingMovies()
            ├─→ Check MongoDB Cache
            │   └─→ If EMPTY: Fetch from MySQL, enrich with movie info, SAVE to cache
            └─→ Return full movie data via protobuf
```

## Caching Strategy

- **Movie Info Service**: MongoDB cache for movie details (HIT/MISS with TheMovieDB fallback)
- **Trending Movies Service**:
  - Startup delay (30s) for service registration
  - MongoDB cache for full trending data
  - Scheduled daily refresh
  - Cache miss triggers fetch + enrichment

## Databases

- **MySQL** (3306): User ratings
- **MongoDB** (27017): Movie cache, trending cache

## Running the Application

1. Start Discovery Server
2. Start other services in any order
3. Services register with Eureka automatically

## Dependencies

- Java 17+
- Spring Boot
- Eureka (Discovery)
- MySQL
- MongoDB
- TheMovieDB API key
