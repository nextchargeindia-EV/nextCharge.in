package com.nextcharge.api.repository;

import com.nextcharge.api.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByStationIdOrderByCreatedAtDesc(UUID stationId);
    Optional<Review> findByCustomerIdAndStationId(UUID customerId, UUID stationId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.station.id = :stationId")
    Double getAverageRatingForStation(@Param("stationId") UUID stationId);
}
