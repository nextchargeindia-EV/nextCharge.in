package com.nextcharge.api.service;

import com.nextcharge.api.dto.BookingRequest;
import com.nextcharge.api.dto.BookingResponse;
import com.nextcharge.api.dto.PaymentVerifyRequest;
import com.nextcharge.api.model.*;
import com.nextcharge.api.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PaymentService paymentService;
    private final EntityManager entityManager;

    public BookingServiceImpl(BookingRepository bookingRepository, ChargerRepository chargerRepository,
                              UserRepository userRepository, PaymentRepository paymentRepository,
                              FleetVehicleRepository fleetVehicleRepository, NotificationRepository notificationRepository,
                              AuditLogRepository auditLogRepository, PaymentService paymentService,
                              EntityManager entityManager) {
        this.bookingRepository = bookingRepository;
        this.chargerRepository = chargerRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.paymentService = paymentService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, UUID customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Charger charger = chargerRepository.findById(request.getChargerId())
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        if (charger.getStatus() == Charger.ChargerStatus.MAINTENANCE || charger.getStatus() == Charger.ChargerStatus.OFFLINE) {
            throw new RuntimeException("Charger is currently unavailable due to " + charger.getStatus());
        }

        // Check for double bookings / overlaps
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getChargerId(), request.getStartTime(), request.getEndTime()
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("This charger slot is already reserved during the requested time.");
        }

        // Calculate expected energy and total cost
        long durationSeconds = Duration.between(request.getStartTime(), request.getEndTime()).toSeconds();
        double durationHours = durationSeconds / 3600.0;
        if (durationHours <= 0) {
            throw new RuntimeException("Booking duration must be positive");
        }

        BigDecimal pricePerKwh = charger.getPricePerKwh();
        BigDecimal speedKw = BigDecimal.valueOf(charger.getChargingSpeedKw());
        BigDecimal expectedEnergy = speedKw.multiply(BigDecimal.valueOf(durationHours));
        BigDecimal totalAmount = expectedEnergy.multiply(pricePerKwh).setScale(2, RoundingMode.HALF_UP);

        Booking booking = Booking.builder()
                .customer(customer)
                .charger(charger)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Booking.BookingStatus.PENDING)
                .totalEnergyKwh(expectedEnergy.setScale(2, RoundingMode.HALF_UP))
                .totalAmount(totalAmount)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(savedBooking)
                .amount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        if (request.getPaymentMethod() == Payment.PaymentMethod.WALLET) {
            if (customer.getWalletBalance().compareTo(totalAmount) < 0) {
                throw new RuntimeException("Insufficient wallet balance. Total required: ₹" + totalAmount);
            }
            // Deduct immediately
            customer.setWalletBalance(customer.getWalletBalance().subtract(totalAmount));
            userRepository.save(customer);

            savedBooking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(savedBooking);

            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId("wallet_txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            paymentRepository.save(payment);

            // Send notification
            notificationRepository.save(Notification.builder()
                    .user(customer)
                    .title("Booking Confirmed")
                    .message("Your booking at " + charger.getStation().getName() + " has been successfully confirmed. Paid via wallet.")
                    .type(Notification.NotificationType.BOOKING)
                    .build());
        } else {
            // Generate Razorpay Order
            String orderId = paymentService.createOrder(totalAmount);
            payment.setTransactionId(orderId);
            paymentRepository.save(payment);
        }

        // Link fleet vehicle if provided
        if (request.getPlateNumber() != null && !request.getPlateNumber().trim().isEmpty()) {
            FleetVehicle vehicle = fleetVehicleRepository.findByPlateNumber(request.getPlateNumber())
                    .orElseThrow(() -> new RuntimeException("Fleet vehicle with plate " + request.getPlateNumber() + " not found"));
            
            // Insert native join relation
            entityManager.createNativeQuery("INSERT INTO fleet_bookings (booking_id, vehicle_id) VALUES (?, ?)")
                    .setParameter(1, savedBooking.getId())
                    .setParameter(2, vehicle.getId())
                    .executeUpdate();
        }

        // Audit Log
        auditLogRepository.save(AuditLog.builder()
                .user(customer)
                .action("CREATE_BOOKING")
                .details("Created slot booking for charger " + charger.getId() + ", amount: ₹" + totalAmount)
                .build());

        return mapToResponse(savedBooking, payment);
    }

    @Override
    @Transactional
    public BookingResponse verifyBookingPayment(PaymentVerifyRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        boolean verified = paymentService.verifyPaymentSignature(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature()
        );

        if (!verified) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment signature verification failed.");
        }

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(request.getRazorpayPaymentId());
        paymentRepository.save(payment);

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        // Notify
        notificationRepository.save(Notification.builder()
                .user(booking.getCustomer())
                .title("Payment Successful")
                .message("Your payment of ₹" + payment.getAmount() + " was processed successfully.")
                .type(Notification.NotificationType.PAYMENT)
                .build());

        return mapToResponse(saved, payment);
    }

    @Override
    @Transactional
    public BookingResponse startCharging(UUID bookingId, UUID customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized booking control");
        }

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Charging cannot start. Booking is " + booking.getStatus());
        }

        Charger charger = booking.getCharger();
        charger.setStatus(Charger.ChargerStatus.CHARGING);
        chargerRepository.save(charger);

        booking.setStatus(Booking.BookingStatus.ACTIVE);
        Booking saved = bookingRepository.save(booking);

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);

        // Audit Log
        auditLogRepository.save(AuditLog.builder()
                .user(booking.getCustomer())
                .action("START_CHARGING")
                .details("Charging session started on charger " + charger.getName())
                .build());

        return mapToResponse(saved, payment);
    }

    @Override
    @Transactional
    public BookingResponse completeCharging(UUID bookingId, BigDecimal finalEnergyKwh) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.ACTIVE) {
            throw new RuntimeException("Cannot complete booking. Status is " + booking.getStatus());
        }

        Charger charger = booking.getCharger();
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        chargerRepository.save(charger);

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        if (finalEnergyKwh != null && finalEnergyKwh.compareTo(BigDecimal.ZERO) > 0) {
            booking.setTotalEnergyKwh(finalEnergyKwh);
            // Recalculate amount if needed, or keep the locked deposit
        }
        Booking saved = bookingRepository.save(booking);

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);

        // Notify
        notificationRepository.save(Notification.builder()
                .user(booking.getCustomer())
                .title("Charging Completed")
                .message("Your charging session at " + charger.getStation().getName() + " is complete. Energy consumed: " + booking.getTotalEnergyKwh() + " kWh.")
                .type(Notification.NotificationType.BOOKING)
                .build());

        // Audit Log
        auditLogRepository.save(AuditLog.builder()
                .user(booking.getCustomer())
                .action("COMPLETE_CHARGING")
                .details("Charging completed, energy: " + booking.getTotalEnergyKwh() + " kWh")
                .build());

        return mapToResponse(saved, payment);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!booking.getCustomer().getId().equals(userId) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Unauthorized cancellation");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED || booking.getStatus() == Booking.BookingStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel an active or completed charging session.");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        if (payment != null && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            // Refund to wallet
            User customer = booking.getCustomer();
            customer.setWalletBalance(customer.getWalletBalance().add(payment.getAmount()));
            userRepository.save(customer);

            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            notificationRepository.save(Notification.builder()
                    .user(customer)
                    .title("Booking Cancelled & Refunded")
                    .message("Your booking was cancelled. Refund of ₹" + payment.getAmount() + " credited back to your NextCharge wallet.")
                    .type(Notification.NotificationType.PAYMENT)
                    .build());
        }

        return mapToResponse(saved, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        return mapToResponse(booking, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByCustomer(UUID customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(b -> {
                    Payment p = paymentRepository.findByBookingId(b.getId()).orElse(null);
                    return mapToResponse(b, p);
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByOwner(UUID ownerId) {
        return bookingRepository.findBookingsByStationOwner(ownerId).stream()
                .map(b -> {
                    Payment p = paymentRepository.findByBookingId(b.getId()).orElse(null);
                    return mapToResponse(b, p);
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(b -> {
                    Payment p = paymentRepository.findByBookingId(b.getId()).orElse(null);
                    return mapToResponse(b, p);
                }).toList();
    }

    private BookingResponse mapToResponse(Booking booking, Payment payment) {
        return BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getName())
                .chargerId(booking.getCharger().getId())
                .chargerName(booking.getCharger().getName())
                .connectorType(booking.getCharger().getConnectorType().name())
                .stationId(booking.getCharger().getStation().getId())
                .stationName(booking.getCharger().getStation().getName())
                .stationAddress(booking.getCharger().getStation().getAddress())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .totalEnergyKwh(booking.getTotalEnergyKwh())
                .totalAmount(booking.getTotalAmount())
                .transactionId(payment != null ? payment.getTransactionId() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
