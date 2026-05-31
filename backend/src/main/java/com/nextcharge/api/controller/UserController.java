package com.nextcharge.api.controller;

import com.nextcharge.api.dto.StationResponse;
import com.nextcharge.api.dto.WalletTopUpRequest;
import com.nextcharge.api.model.Favorite;
import com.nextcharge.api.model.Notification;
import com.nextcharge.api.model.User;
import com.nextcharge.api.repository.FavoriteRepository;
import com.nextcharge.api.repository.UserRepository;
import com.nextcharge.api.security.UserPrincipal;
import com.nextcharge.api.service.NotificationService;
import com.nextcharge.api.service.StationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final StationService stationService;
    private final NotificationService notificationService;

    public UserController(UserRepository userRepository, FavoriteRepository favoriteRepository,
                          StationService stationService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.stationService = stationService;
        this.notificationService = notificationService;
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String name,
            @RequestParam String phone) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(name);
        user.setPhone(phone);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/wallet/topup")
    public ResponseEntity<User> topUpWallet(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WalletTopUpRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setWalletBalance(user.getWalletBalance().add(request.getAmount()));
        User saved = userRepository.save(user);
        saved.setPassword(null);

        notificationService.createGeneralNotification(
                saved.getId(),
                "Wallet Credited",
                "Your NextCharge wallet has been credited with ₹" + request.getAmount() + ". New Balance: ₹" + saved.getWalletBalance()
        );

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(principal.getId()));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/favorites/{stationId}")
    @Transactional
    public ResponseEntity<String> toggleFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID stationId) {
        Favorite.FavoriteId favId = new Favorite.FavoriteId(principal.getId(), stationId);
        boolean exists = favoriteRepository.existsById(favId);
        if (exists) {
            favoriteRepository.deleteById(favId);
            return ResponseEntity.ok("REMOVED");
        } else {
            favoriteRepository.save(Favorite.builder()
                    .customerId(principal.getId())
                    .stationId(stationId)
                    .build());
            return ResponseEntity.ok("ADDED");
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<StationResponse>> getFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        List<Favorite> favorites = favoriteRepository.findByCustomerId(principal.getId());
        List<StationResponse> list = favorites.stream()
                .map(fav -> stationService.getStationById(fav.getStationId()))
                .toList();
        return ResponseEntity.ok(list);
    }
}
