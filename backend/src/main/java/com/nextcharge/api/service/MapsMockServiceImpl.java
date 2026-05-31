package com.nextcharge.api.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class MapsMockServiceImpl implements MapsService {

    @Override
    public String getDistanceAndDuration(BigDecimal startLat, BigDecimal startLng, BigDecimal endLat, BigDecimal endLng) {
        if (startLat == null || startLng == null || endLat == null || endLng == null) {
            return "0.0 km (0 mins)";
        }
        
        // Calculate Euclidean distance approximation
        double latDiff = startLat.subtract(endLat).doubleValue();
        double lngDiff = startLng.subtract(endLng).doubleValue();
        double distanceDegrees = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
        
        // 1 degree is roughly 111 km
        double distanceKm = distanceDegrees * 111.0;
        if (distanceKm < 0.2) {
            distanceKm = 0.5; // default fallback minimum
        }
        
        // Simulates typical Indian city transit speed (25 km/h average due to traffic)
        double durationMins = (distanceKm / 25.0) * 60.0;
        
        return String.format("%.1f km (%d mins)", distanceKm, (int) Math.round(durationMins));
    }
}
