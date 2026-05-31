package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
