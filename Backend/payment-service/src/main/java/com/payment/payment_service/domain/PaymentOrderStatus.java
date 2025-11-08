package com.payment.payment_service.domain;

public enum PaymentOrderStatus {
    PENDING,
    REQUIRES_ACTION,
    SUCCEEDED,
    CANCELED,
    FAILED
}
