package com.nextcharge.api.controller;

import com.nextcharge.api.dto.ChargerResponse;
import com.nextcharge.api.dto.StationRequest;
import com.nextcharge.api.dto.StationResponse;
import com.nextcharge.api.model.Charger;
import com.nextcharge.api.model.Station;
import com.nextcharge.api.model.StationImage;
import com.nextcharge.api.repository.StationImageRepository;
import com.nextcharge.api.repository.StationRepository;
import com.nextcharge.api.security.UserPrincipal;
import com.nextcharge.api.service.StationService;
import com.nextcharge.api.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;
    private final StorageService storageService;
    private final StationImageRepository stationImageRepository;
    private final StationRepository stationRepository;

    public StationController(StationService stationService, StorageService storageService,
                             StationImageRepository stationImageRepository, StationRepository stationRepository) {
        this.stationService = stationService;
        this.storageService = storageService;
        this.stationImageRepository = stationImageRepository;
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> getStations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minLat,
            @RequestParam(required = false) BigDecimal maxLat,
            @RequestParam(required = false) BigDecimal minLng,
            @RequestParam(required = false) BigDecimal maxLng) {
        return ResponseEntity.ok(stationService.getAllStations(search, minLat, maxLat, minLng, maxLng));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStationById(@PathVariable UUID id) {
        return ResponseEntity.ok(stationService.getStationById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<StationResponse> createStation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StationRequest request) {
        return ResponseEntity.ok(stationService.createStation(request, principal.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<StationResponse> updateStation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody StationRequest request) {
        return ResponseEntity.ok(stationService.updateStation(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteStation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        stationService.deleteStation(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // Charger Management
    @PostMapping("/{id}/chargers")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<ChargerResponse> addCharger(
            @PathVariable UUID id,
            @RequestParam String name,
            @RequestParam Charger.ConnectorType connectorType,
            @RequestParam Integer speedKw,
            @RequestParam BigDecimal pricePerKwh) {
        return ResponseEntity.ok(stationService.addCharger(id, name, connectorType, speedKw, pricePerKwh));
    }

    @DeleteMapping("/chargers/{chargerId}")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteCharger(@PathVariable UUID chargerId) {
        stationService.deleteCharger(chargerId);
        return ResponseEntity.noContent().build();
    }

    // Live Heartbeat/Connectivity Status updater
    @PatchMapping("/chargers/{chargerId}/status")
    public ResponseEntity<ChargerResponse> updateChargerStatus(
            @PathVariable UUID chargerId,
            @RequestParam Charger.ChargerStatus status) {
        return ResponseEntity.ok(stationService.updateChargerStatus(chargerId, status));
    }

    // Station Live Active Status Toggler (blinking active status)
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<StationResponse> toggleStationActive(
            @PathVariable UUID id,
            @RequestParam boolean isActive) {
        return ResponseEntity.ok(stationService.toggleStationActive(id, isActive));
    }

    // Image Upload
    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<StationImage> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        
        String url = storageService.store(file);
        
        StationImage image = StationImage.builder()
                .station(station)
                .imageUrl(url)
                .build();
                
        return ResponseEntity.ok(stationImageRepository.save(image));
    }
    
    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<List<StationResponse>> getMyStations(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(stationService.getStationsByOwner(principal.getId()));
    }
}
