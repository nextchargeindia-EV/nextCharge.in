package com.nextcharge.api.controller;

import com.nextcharge.api.dto.BookingRequest;
import com.nextcharge.api.dto.BookingResponse;
import com.nextcharge.api.dto.PaymentVerifyRequest;
import com.nextcharge.api.security.UserPrincipal;
import com.nextcharge.api.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request, principal.getId()));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<BookingResponse> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(bookingService.verifyBookingPayment(request));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<BookingResponse> startCharging(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.startCharging(id, principal.getId()));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeCharging(
            @PathVariable UUID id,
            @RequestParam(required = false) BigDecimal finalEnergyKwh) {
        return ResponseEntity.ok(bookingService.completeCharging(id, finalEnergyKwh));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getBookingsByCustomer(principal.getId()));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('STATION_OWNER', 'ADMIN')")
    public ResponseEntity<List<BookingResponse>> getOwnerBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getBookingsByOwner(principal.getId()));
    }
}
