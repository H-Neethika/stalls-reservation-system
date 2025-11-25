package com.payment.payment_service.messaging.producer;

import com.payment.payment_service.messaging.event.PaymentSucceededEvent;
import com.payment.payment_service.model.PaymentOrder;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentSucceededEvent> kafkaTemplate;

    @Value("${app.kafka.topics.payment-success:payment.success}")
    private String paymentSuccessTopic;

    public void publishPaymentSucceeded(PaymentOrder order) {
        PaymentSucceededEvent event = PaymentSucceededEvent.builder()
                .paymentOrderId(order.getId())
                .reservationId(order.getReservationId())
                .amount(order.getAmount())
                .originalAmount(order.getOriginalAmount())
                .convertedStripeAmount(order.getConvertedStripeAmount())
                .currency(order.getCurrency())
                .occurredAt(Instant.now())
                .build();

        CompletableFuture<SendResult<String, PaymentSucceededEvent>> future =
                kafkaTemplate.send(paymentSuccessTopic, event.getReservationId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish payment success event for reservationId={}", event.getReservationId(), ex);
            } else {
                log.info("Published payment success event for reservationId={} on topic={}",
                        event.getReservationId(), paymentSuccessTopic);
            }
        });
    }
}
