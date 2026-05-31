package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.GENERAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    public enum NotificationType {
        BOOKING, PAYMENT, STATION_STATUS, PROMO, GENERAL
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
