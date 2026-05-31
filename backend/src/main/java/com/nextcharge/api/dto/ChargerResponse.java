package com.nextcharge.api.dto;

import com.nextcharge.api.model.Charger.ConnectorType;
import com.nextcharge.api.model.Charger.ChargerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargerResponse {
    private UUID id;
    private String name;
    private ConnectorType connectorType;
    private Integer chargingSpeedKw;
    private ChargerStatus status;
    private BigDecimal pricePerKwh;
}
