package com.nextcharge.api.repository;

import com.nextcharge.api.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<Booking> findByChargerId(UUID chargerId);

    @Query("SELECT b FROM Booking b WHERE b.charger.id = :chargerId AND b.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') AND " +
           "(b.startTime < :endTime AND b.endTime > :startTime)")
    List<Booking> findConflictingBookings(
            @Param("chargerId") UUID chargerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT b FROM Booking b JOIN b.charger c WHERE c.station.owner.id = :ownerId ORDER BY b.createdAt DESC")
    List<Booking> findBookingsByStationOwner(@Param("ownerId") UUID ownerId);

    @Query("SELECT b FROM Booking b JOIN b.charger c WHERE c.station.id = :stationId ORDER BY b.createdAt DESC")
    List<Booking> findBookingsByStation(@Param("stationId") UUID stationId);
}
