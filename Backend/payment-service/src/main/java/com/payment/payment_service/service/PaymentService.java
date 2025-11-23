package com.payment.payment_service.service;

import com.payment.payment_service.payload.request.CreatePaymentRequest;
import com.payment.payment_service.payload.response.PaymentIntentResponse;
import com.payment.payment_service.payload.response.PaymentOrderResponse;

public interface PaymentService {
    PaymentIntentResponse createPaymentIntent(CreatePaymentRequest request);

    PaymentOrderResponse getLatestOrderByReservationId(Long reservationId);

    void handleStripeWebhook(String payload, String signatureHeader);

    // Test method to simulate payment completion
    void testCompletePayment(Long orderId);

    // Method to get payment order by ID
    PaymentOrderResponse getPaymentOrderById(Long orderId);
}

