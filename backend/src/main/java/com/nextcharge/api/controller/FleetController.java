package com.nextcharge.api.controller;

import com.nextcharge.api.model.FleetVehicle;
import com.nextcharge.api.security.UserPrincipal;
import com.nextcharge.api.service.FleetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fleet")
@PreAuthorize("hasRole('FLEET_MANAGER')")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @PostMapping("/vehicles")
    public ResponseEntity<FleetVehicle> addVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String plateNumber,
            @RequestParam String model,
            @RequestParam String driverName) {
        return ResponseEntity.ok(fleetService.addVehicle(principal.getId(), plateNumber, model, driverName));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<FleetVehicle>> getMyVehicles(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(fleetService.getVehiclesByManager(principal.getId()));
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> removeVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        fleetService.removeVehicle(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
