package com.payment.payment_service.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "reservationId is required")
    private Long reservationId;

    @NotNull(message = "totalAmount is required")
    @Min(value = 1, message = "totalAmount must be positive (in smallest currency unit)")
    private Long totalAmount;

    // Optional; defaults from configuration if not provided
    private String currency;
}

