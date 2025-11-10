package com.payment.payment_service.repository;

import com.payment.payment_service.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByStripePaymentIntentId(String paymentIntentId);

    Optional<PaymentOrder> findTopByReservationIdOrderByCreatedAtDesc(Long reservationId);

    Optional<PaymentOrder> findBySessionId(String sessionId);
}
