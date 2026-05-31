package com.nextcharge.api.service;

import com.nextcharge.api.dto.StationRequest;
import com.nextcharge.api.dto.StationResponse;
import com.nextcharge.api.dto.ChargerResponse;
import com.nextcharge.api.model.Charger.ConnectorType;
import com.nextcharge.api.model.Charger.ChargerStatus;
import com.nextcharge.api.model.Station;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StationService {
    StationResponse createStation(StationRequest request, UUID ownerId);
    StationResponse updateStation(UUID stationId, StationRequest request, UUID ownerId);
    StationResponse getStationById(UUID stationId);
    List<StationResponse> getAllStations(String search, BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng);
    List<StationResponse> getStationsByOwner(UUID ownerId);
    void deleteStation(UUID stationId, UUID ownerId);
    
    // Charger operations
    ChargerResponse addCharger(UUID stationId, String name, ConnectorType type, Integer speed, BigDecimal priceKwh);
    void deleteCharger(UUID chargerId);
    ChargerResponse updateChargerStatus(UUID chargerId, ChargerStatus status);
    
    // Admin operations
    StationResponse approveStation(UUID stationId, Station.ApprovalStatus status);
    
    // Live Active Status heartbeat controls
    StationResponse toggleStationActive(UUID stationId, boolean isActive);
}
