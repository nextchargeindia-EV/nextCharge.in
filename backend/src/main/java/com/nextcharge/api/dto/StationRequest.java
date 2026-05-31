package com.nextcharge.api.dto;

import com.nextcharge.api.model.StationAmenity.AmenityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StationRequest {
    @NotBlank(message = "Station name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private String operatingHours = "24/7";

    private List<AmenityType> amenities;

    private List<String> imageUrls;
}
