package com.nextcharge.api.repository;

import com.nextcharge.api.model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, UUID> {
    List<Charger> findByStationId(UUID stationId);
}
