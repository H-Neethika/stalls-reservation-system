package com.booking.booking_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequest {

  @NotNull(message = "reservationId is required")
  private Long reservationId;

  // Human-readable amount, e.g. 500.00 or 50.00
  @NotNull(message = "totalAmount is required")
  @DecimalMin(value = "0.01", inclusive = true, message = "totalAmount must be at least 0.01")
  private BigDecimal totalAmount;

  // Optional; defaults from configuration if not provided
  private String currency; // "LKR" or "USD"

}
