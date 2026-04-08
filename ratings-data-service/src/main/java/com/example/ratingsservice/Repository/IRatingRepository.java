package com.example.ratingsservice.Repository;

import com.example.ratingsservice.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IRatingRepository extends JpaRepository<Rating, Integer> {
    List<Rating> findByUserId(String userId);

    @Query("SELECT r.movieId as movieId, AVG(r.rating) as averageRating " +
            "FROM Rating r " +
            "GROUP BY r.movieId " +
            "ORDER BY AVG(r.rating) DESC")
    List<Object[]> findTopMoviesByAverageRating(Pageable pageable);
}