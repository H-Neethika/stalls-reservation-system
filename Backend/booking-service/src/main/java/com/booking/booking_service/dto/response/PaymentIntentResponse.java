package com.booking.booking_service.dto.response;

import com.booking.booking_service.enums.PaymentOrderStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
