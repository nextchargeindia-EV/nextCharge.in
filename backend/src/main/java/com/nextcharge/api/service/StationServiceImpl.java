package com.nextcharge.api.service;

import com.nextcharge.api.dto.ChargerResponse;
import com.nextcharge.api.dto.StationRequest;
import com.nextcharge.api.dto.StationResponse;
import com.nextcharge.api.model.*;
import com.nextcharge.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public StationServiceImpl(StationRepository stationRepository, ChargerRepository chargerRepository,
                              UserRepository userRepository, ReviewRepository reviewRepository) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public StationResponse createStation(StationRequest request, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Station station = Station.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .operatingHours(request.getOperatingHours())
                .owner(owner)
                .status(Station.ApprovalStatus.PENDING_APPROVAL)
                .isActive(true)
                .build();

        Station saved = stationRepository.save(station);

        // Add amenities
        if (request.getAmenities() != null) {
            List<StationAmenity> amenities = request.getAmenities().stream()
                    .map(a -> StationAmenity.builder()
                            .station(saved)
                            .amenityName(a)
                            .build())
                    .collect(Collectors.toList());
            saved.setAmenities(amenities);
        }

        // Add images
        if (request.getImageUrls() != null) {
            List<StationImage> images = request.getImageUrls().stream()
                    .map(url -> StationImage.builder()
                            .station(saved)
                            .imageUrl(url)
                            .build())
                    .collect(Collectors.toList());
            saved.setImages(images);
        }

        Station finalSaved = stationRepository.save(saved);
        return mapToResponse(finalSaved);
    }

    @Override
    @Transactional
    public StationResponse updateStation(UUID stationId, StationRequest request, UUID ownerId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        if (!station.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized action on this station");
        }

        station.setName(request.getName());
        station.setDescription(request.getDescription());
        station.setAddress(request.getAddress());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setOperatingHours(request.getOperatingHours());

        // Update amenities (replace current)
        station.getAmenities().clear();
        if (request.getAmenities() != null) {
            request.getAmenities().forEach(a -> station.getAmenities().add(
                    StationAmenity.builder().station(station).amenityName(a).build()
            ));
        }

        // Update images (replace current)
        station.getImages().clear();
        if (request.getImageUrls() != null) {
            request.getImageUrls().forEach(url -> station.getImages().add(
                    StationImage.builder().station(station).imageUrl(url).build()
            ));
        }

        Station updated = stationRepository.save(station);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public StationResponse getStationById(UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        return mapToResponse(station);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> getAllStations(String search, BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng) {
        List<Station> stations;
        if (minLat != null && maxLat != null && minLng != null && maxLng != null) {
            stations = stationRepository.findStationsInBounds(minLat, maxLat, minLng, maxLng);
        } else if (search != null && !search.trim().isEmpty()) {
            stations = stationRepository.searchStations(search);
        } else {
            stations = stationRepository.findByStatusAndIsActive(Station.ApprovalStatus.APPROVED, true);
        }
        return stations.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> getStationsByOwner(UUID ownerId) {
        return stationRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteStation(UUID stationId, UUID ownerId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        if (!station.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized action");
        }

        stationRepository.delete(station);
    }

    @Override
    @Transactional
    public ChargerResponse addCharger(UUID stationId, String name, Charger.ConnectorType type, Integer speed, BigDecimal priceKwh) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        Charger charger = Charger.builder()
                .station(station)
                .name(name)
                .connectorType(type)
                .chargingSpeedKw(speed)
                .pricePerKwh(priceKwh)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        Charger saved = chargerRepository.save(charger);
        return mapToChargerResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCharger(UUID chargerId) {
        chargerRepository.deleteById(chargerId);
    }

    @Override
    @Transactional
    public ChargerResponse updateChargerStatus(UUID chargerId, Charger.ChargerStatus status) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new RuntimeException("Charger not found"));
        charger.setStatus(status);
        Charger saved = chargerRepository.save(charger);
        return mapToChargerResponse(saved);
    }

    @Override
    @Transactional
    public StationResponse approveStation(UUID stationId, Station.ApprovalStatus status) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        station.setStatus(status);
        Station saved = stationRepository.save(station);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public StationResponse toggleStationActive(UUID stationId, boolean isActive) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        station.setActive(isActive);
        Station saved = stationRepository.save(station);
        return mapToResponse(saved);
    }

    private StationResponse mapToResponse(Station station) {
        List<String> imageUrls = station.getImages() != null ?
                station.getImages().stream().map(StationImage::getImageUrl).toList() : new ArrayList<>();

        List<String> amenities = station.getAmenities() != null ?
                station.getAmenities().stream().map(a -> a.getAmenityName().name()).toList() : new ArrayList<>();

        List<ChargerResponse> chargers = station.getChargers() != null ?
                station.getChargers().stream().map(this::mapToChargerResponse).toList() : new ArrayList<>();

        Double avgRating = reviewRepository.getAverageRatingForStation(station.getId());
        if (avgRating == null) avgRating = 0.0;

        return StationResponse.builder()
                .id(station.getId())
                .ownerId(station.getOwner().getId())
                .name(station.getName())
                .description(station.getDescription())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .operatingHours(station.getOperatingHours())
                .status(station.getStatus())
                .isActive(station.isActive())
                .imageUrls(imageUrls)
                .amenities(amenities)
                .chargers(chargers)
                .averageRating(avgRating)
                .createdAt(station.getCreatedAt())
                .build();
    }

    private ChargerResponse mapToChargerResponse(Charger charger) {
        return ChargerResponse.builder()
                .id(charger.getId())
                .name(charger.getName())
                .connectorType(charger.getConnectorType())
                .chargingSpeedKw(charger.getChargingSpeedKw())
                .status(charger.getStatus())
                .pricePerKwh(charger.getPricePerKwh())
                .build();
    }
}
