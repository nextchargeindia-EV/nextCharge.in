package com.nextcharge.api.dto;

import com.nextcharge.api.model.Station.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String operatingHours;
    private ApprovalStatus status;
    private boolean isActive; // glowing live active/inactive status indicator
    private List<String> imageUrls;
    private List<String> amenities;
    private List<ChargerResponse> chargers;
    private Double averageRating;
    private ZonedDateTime createdAt;
}
