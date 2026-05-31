package com.nextcharge.api.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RazorpayMockServiceImpl implements PaymentService {

    @Override
    public String createOrder(BigDecimal amount) {
        // Simulates Razorpay order ID creation (e.g. order_EHDhuwje292)
        String randomId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "order_" + randomId;
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        // In local/mock mode, we accept all signatures to allow testing checkout out-of-the-box.
        // In production, we would use Razorpay's HMAC SHA256 signature verification utility.
        return orderId != null && paymentId != null && signature != null;
    }
}
