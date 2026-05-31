package com.nextcharge.api.service;

import java.math.BigDecimal;

public interface MapsService {
    String getDistanceAndDuration(BigDecimal startLat, BigDecimal startLng, BigDecimal endLat, BigDecimal endLng);
}
