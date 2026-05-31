package com.nextcharge.api.repository;

import com.nextcharge.api.model.FleetVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FleetVehicleRepository extends JpaRepository<FleetVehicle, UUID> {
    List<FleetVehicle> findByFleetManagerId(UUID fleetManagerId);
    boolean existsByPlateNumber(String plateNumber);
}
