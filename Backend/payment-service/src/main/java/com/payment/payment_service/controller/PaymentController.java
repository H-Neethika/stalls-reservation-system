package com.payment.payment_service.controller;

import com.payment.payment_service.payload.request.CreatePaymentRequest;
import com.payment.payment_service.payload.response.PaymentIntentResponse;
import com.payment.payment_service.payload.response.PaymentOrderResponse;
import com.payment.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentIntentResponse response = paymentService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/by-reservation/{reservationId}")
    public ResponseEntity<PaymentOrderResponse> getLatestOrder(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getLatestOrderByReservationId(reservationId));
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload, @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("received");
    }
}
