package com.nextcharge.api.dto;

import com.nextcharge.api.model.User.Role;
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
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private BigDecimal walletBalance;
}
