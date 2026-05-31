package com.nextcharge.api.controller;

import com.nextcharge.api.dto.AuthRequest;
import com.nextcharge.api.dto.AuthResponse;
import com.nextcharge.api.dto.RegisterRequest;
import com.nextcharge.api.model.User;
import com.nextcharge.api.security.UserPrincipal;
import com.nextcharge.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getUserById(principal.getId());
        // Do not return password hash
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}
