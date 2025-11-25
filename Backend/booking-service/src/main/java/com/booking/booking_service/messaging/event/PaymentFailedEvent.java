package com.booking.booking_service.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentFailedEvent {
    private Long reservationId;
    private Long paymentOrderId;
    private Long amount;
    private BigDecimal originalAmount;
    private Long convertedStripeAmount;
    private String currency;
    private Instant occurredAt;
    private String reason;
}
