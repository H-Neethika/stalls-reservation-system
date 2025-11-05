package com.payment.payment_service.payload.response;

import com.payment.payment_service.domain.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponse {
    private Long orderId;
    private String sessionId;
    private String paymentIntentId;
    private String paymentUrl;
    private String currency; // "LKR" or "USD"
    private BigDecimal originalAmount; // e.g., 500.00
    private Long convertedStripeAmount; // e.g., 50000
    private PaymentOrderStatus status;
}
