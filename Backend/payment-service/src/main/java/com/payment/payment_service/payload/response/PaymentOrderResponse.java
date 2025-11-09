package com.payment.payment_service.payload.response;

import com.payment.payment_service.domain.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {
    private Long orderId;
    private Long reservationId;
    private Long amount;
    private String currency;
    private String paymentIntentId;
    private PaymentOrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

