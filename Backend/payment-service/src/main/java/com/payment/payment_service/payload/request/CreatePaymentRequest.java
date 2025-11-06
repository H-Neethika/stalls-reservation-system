package com.payment.payment_service.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
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
