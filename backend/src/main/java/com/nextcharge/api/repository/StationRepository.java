package com.nextcharge.api.repository;

import com.nextcharge.api.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<Station, UUID> {
    List<Station> findByOwnerId(UUID ownerId);
    List<Station> findByStatus(Station.ApprovalStatus status);
    List<Station> findByStatusAndIsActive(Station.ApprovalStatus status, boolean isActive);

    @Query("SELECT s FROM Station s WHERE s.status = 'APPROVED' AND s.isActive = true AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Station> searchStations(@Param("query") String query);

    @Query("SELECT s FROM Station s WHERE s.status = 'APPROVED' AND s.isActive = true AND " +
           "s.latitude BETWEEN :minLat AND :maxLat AND s.longitude BETWEEN :minLng AND :maxLng")
    List<Station> findStationsInBounds(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng
    );
}
