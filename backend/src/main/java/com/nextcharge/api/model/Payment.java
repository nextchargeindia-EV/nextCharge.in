package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "booking")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    public enum PaymentMethod {
        UPI, CARD, NETBANKING, WALLET
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
