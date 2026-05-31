package com.nextcharge.api.dto;

import com.nextcharge.api.model.Booking.BookingStatus;
import com.nextcharge.api.model.Payment.PaymentStatus;
import com.nextcharge.api.model.Payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID chargerId;
    private String chargerName;
    private String connectorType;
    private UUID stationId;
    private String stationName;
    private String stationAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private BigDecimal totalEnergyKwh;
    private BigDecimal totalAmount;
    private String transactionId;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private ZonedDateTime createdAt;
}
