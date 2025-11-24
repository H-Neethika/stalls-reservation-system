package com.payment.payment_service.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentSucceededEvent {
    Long reservationId;
    Long paymentOrderId;
    Long amount;
    BigDecimal originalAmount;
    Long convertedStripeAmount;
    String currency;
    Instant occurredAt;
}
