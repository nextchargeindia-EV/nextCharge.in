package com.nextcharge.api.service;

import com.nextcharge.api.model.FleetVehicle;
import java.util.List;
import java.util.UUID;

public interface FleetService {
    FleetVehicle addVehicle(UUID managerId, String plateNumber, String model, String driverName);
    List<FleetVehicle> getVehiclesByManager(UUID managerId);
    void removeVehicle(UUID vehicleId, UUID managerId);
}
