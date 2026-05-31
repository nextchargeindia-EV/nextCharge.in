package com.nextcharge.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletTopUpRequest {
    @NotNull(message = "Top up amount is required")
    @DecimalMin(value = "10.00", message = "Minimum top up amount is ₹10.00")
    private BigDecimal amount;
}
