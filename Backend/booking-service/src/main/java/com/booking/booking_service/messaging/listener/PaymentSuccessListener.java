package com.booking.booking_service.messaging.listener;

import com.booking.booking_service.messaging.event.PaymentSucceededEvent;
import com.booking.booking_service.messaging.producer.ReservationBookedEventProducer;
import com.booking.booking_service.response.PaymentSuccessResponse;
import com.booking.booking_service.service.ExhibitionStallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessListener {

    private final ExhibitionStallService exhibitionStallService;
    private final ReservationBookedEventProducer reservationBookedEventProducer;

    @KafkaListener(topics = "${app.kafka.topics.payment-success:payment.success}",
            containerFactory = "paymentSuccessKafkaListenerContainerFactory")
    public void handlePaymentSuccess(@Payload PaymentSucceededEvent event) {
        if (event == null) {
            log.warn("Received null payment success event");
            return;
        }

        log.info("Processing payment success event for reservationId={} orderId={}",
                event.getReservationId(), event.getPaymentOrderId());

        try {
            PaymentSuccessResponse response = exhibitionStallService.updateStallBookingStatus(event.getReservationId());
            reservationBookedEventProducer.publishReservationBooked(response);
        } catch (Exception ex) {
            log.error("Failed to process payment success event for reservationId={}", event.getReservationId(), ex);
            throw ex;
        }
    }
}
