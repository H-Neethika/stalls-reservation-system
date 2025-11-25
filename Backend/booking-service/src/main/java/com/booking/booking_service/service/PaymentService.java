package com.booking.booking_service.service;

import com.booking.booking_service.dto.request.CreatePaymentRequest;
import com.booking.booking_service.dto.response.PaymentIntentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE", path = "/api/payment")
public interface PaymentService {
    @PostMapping("/intent")
    PaymentIntentResponse createPaymentIntent(@RequestBody CreatePaymentRequest request);
}
