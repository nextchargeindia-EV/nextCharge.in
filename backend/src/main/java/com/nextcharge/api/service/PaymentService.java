package com.nextcharge.api.service;

import java.math.BigDecimal;

public interface PaymentService {
    String createOrder(BigDecimal amount);
    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);
}
