package com.nextcharge.api.controller;

import com.nextcharge.api.dto.StationResponse;
import com.nextcharge.api.model.AuditLog;
import com.nextcharge.api.model.Station;
import com.nextcharge.api.model.User;
import com.nextcharge.api.repository.StationRepository;
import com.nextcharge.api.repository.UserRepository;
import com.nextcharge.api.service.AuditLogService;
import com.nextcharge.api.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final StationService stationService;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminController(StationService stationService, StationRepository stationRepository,
                           UserRepository userRepository, AuditLogService auditLogService) {
        this.stationService = stationService;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/stations/pending")
    public ResponseEntity<List<Station>> getPendingStations() {
        return ResponseEntity.ok(stationRepository.findByStatus(Station.ApprovalStatus.PENDING_APPROVAL));
    }

    @PostMapping("/stations/{id}/approve")
    public ResponseEntity<StationResponse> approveStation(
            @PathVariable UUID id,
            @RequestParam Station.ApprovalStatus status) {
        return ResponseEntity.ok(stationService.approveStation(id, status));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Hide password hashes
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam User.Status status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}
