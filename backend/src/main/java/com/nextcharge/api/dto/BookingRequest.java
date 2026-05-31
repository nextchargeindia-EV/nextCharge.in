package com.nextcharge.api.dto;

import com.nextcharge.api.model.Payment.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull(message = "Charger ID is required")
    private UUID chargerId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Optional fields for fleet vehicles
    private String plateNumber;
}
