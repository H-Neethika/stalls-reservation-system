package com.payment.payment_service.payload.response;

import com.payment.payment_service.domain.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponse {
    private Long orderId;
    private String sessionId;
    private String paymentIntentId;
    private String paymentUrl;
    private PaymentOrderStatus status;
}
