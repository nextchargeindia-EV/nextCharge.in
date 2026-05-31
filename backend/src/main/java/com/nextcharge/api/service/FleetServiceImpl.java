package com.nextcharge.api.service;

import com.nextcharge.api.model.FleetVehicle;
import com.nextcharge.api.model.User;
import com.nextcharge.api.repository.FleetVehicleRepository;
import com.nextcharge.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FleetServiceImpl implements FleetService {

    private final FleetVehicleRepository fleetVehicleRepository;
    private final UserRepository userRepository;

    public FleetServiceImpl(FleetVehicleRepository fleetVehicleRepository, UserRepository userRepository) {
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FleetVehicle addVehicle(UUID managerId, String plateNumber, String model, String driverName) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Fleet manager not found"));

        if (manager.getRole() != User.Role.FLEET_MANAGER) {
            throw new RuntimeException("Only FLEET_MANAGER accounts can manage vehicles");
        }

        if (fleetVehicleRepository.existsByPlateNumber(plateNumber)) {
            throw new RuntimeException("Plate number already registered");
        }

        FleetVehicle vehicle = FleetVehicle.builder()
                .fleetManager(manager)
                .plateNumber(plateNumber)
                .model(model)
                .driverName(driverName)
                .build();

        return fleetVehicleRepository.save(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FleetVehicle> getVehiclesByManager(UUID managerId) {
        return fleetVehicleRepository.findByFleetManagerId(managerId);
    }

    @Override
    @Transactional
    public void removeVehicle(UUID vehicleId, UUID managerId) {
        FleetVehicle vehicle = fleetVehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getFleetManager().getId().equals(managerId)) {
            throw new RuntimeException("Unauthorized vehicle control");
        }

        fleetVehicleRepository.delete(vehicle);
    }
}
