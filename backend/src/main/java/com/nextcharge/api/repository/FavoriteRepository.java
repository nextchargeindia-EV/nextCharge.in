package com.nextcharge.api.repository;

import com.nextcharge.api.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Favorite.FavoriteId> {
    List<Favorite> findByCustomerId(UUID customerId);
    boolean existsByCustomerIdAndStationId(UUID customerId, UUID stationId);
    void deleteByCustomerIdAndStationId(UUID customerId, UUID stationId);
}
