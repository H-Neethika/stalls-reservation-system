package com.booking.booking_service.service;

import com.booking.booking_service.response.CreatePaymentRequest;
import com.booking.booking_service.response.PaymentIntentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "PAYMENT-SERVICE", url = "http://localhost:8083/")
public interface PaymentService {

  @PostMapping("/api/payment/intent")
  public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
      @RequestBody CreatePaymentRequest request);

}
