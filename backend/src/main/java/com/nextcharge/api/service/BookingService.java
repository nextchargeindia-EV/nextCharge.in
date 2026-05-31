package com.nextcharge.api.service;

import com.nextcharge.api.dto.BookingRequest;
import com.nextcharge.api.dto.BookingResponse;
import com.nextcharge.api.dto.PaymentVerifyRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, UUID customerId);
    BookingResponse verifyBookingPayment(PaymentVerifyRequest request);
    BookingResponse startCharging(UUID bookingId, UUID customerId);
    BookingResponse completeCharging(UUID bookingId, BigDecimal finalEnergyKwh);
    BookingResponse cancelBooking(UUID bookingId, UUID userId);
    
    BookingResponse getBookingById(UUID bookingId);
    List<BookingResponse> getBookingsByCustomer(UUID customerId);
    List<BookingResponse> getBookingsByOwner(UUID ownerId);
    List<BookingResponse> getAllBookings();
}
