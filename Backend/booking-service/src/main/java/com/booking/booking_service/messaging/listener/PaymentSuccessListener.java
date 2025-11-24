package com.booking.booking_service.messaging.listener;

import com.booking.booking_service.messaging.event.PaymentSucceededEvent;
import com.booking.booking_service.messaging.producer.ReservationBookedEventProducer;
import com.booking.booking_service.dto.response.PaymentSuccessResponse;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.enums.ReservationStatus;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.dto.request.UpdateStallStatusRequest;
import com.booking.booking_service.service.ExhibitionServiceClient;
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
    private final ExhibitionServiceClient exhibitionServiceClient;
    private final ReservationRepository reservationRepository;
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
            // Update reservation status to CONFIRMED
            Reservation reservation = reservationRepository.findById(event.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + event.getReservationId()));
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            // Mark stalls as RESERVED in exhibition-service
            UpdateStallStatusRequest statusRequest = new UpdateStallStatusRequest();
            statusRequest.setStallIds(reservation.getStallIds());
            statusRequest.setBookingStatus("RESERVED");
            try {
                exhibitionServiceClient.updateBookingStatus(statusRequest);
            } catch (Exception ex) {
                log.warn("Failed to update stall statuses for reservationId={}: {}", reservation.getId(), ex.getMessage());
            }

            PaymentSuccessResponse response = exhibitionStallService.updateStallBookingStatus(event.getReservationId());
            reservationBookedEventProducer.publishReservationBooked(response);
        } catch (Exception ex) {
            log.error("Failed to process payment success event for reservationId={}", event.getReservationId(), ex);
            throw ex;
        }
    }
}
