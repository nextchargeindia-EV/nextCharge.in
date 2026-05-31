package com.nextcharge.api.service;

import com.nextcharge.api.dto.AuthRequest;
import com.nextcharge.api.dto.AuthResponse;
import com.nextcharge.api.dto.RegisterRequest;
import com.nextcharge.api.model.User;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    User getUserById(UUID userId);
    User getUserByEmail(String email);
    User updateProfile(UUID userId, String name, String phone);
}
