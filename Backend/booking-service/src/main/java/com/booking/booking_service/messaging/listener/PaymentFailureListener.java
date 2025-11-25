package com.booking.booking_service.messaging.listener;

import com.booking.booking_service.messaging.event.PaymentFailedEvent;
import com.booking.booking_service.enums.ReservationStatus;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.dto.request.UpdateStallStatusRequest;
import com.booking.booking_service.service.ExhibitionServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureListener {

    private final ReservationRepository reservationRepository;
    private final ExhibitionServiceClient exhibitionServiceClient;

    @KafkaListener(topics = "${app.kafka.topics.payment-failed:payment.failed}",
            containerFactory = "paymentFailedKafkaListenerContainerFactory")
    public void handlePaymentFailure(@Payload PaymentFailedEvent event) {
        if (event == null) {
            log.warn("Received null payment failure event");
            return;
        }
        log.info("Processing payment failure event for reservationId={} orderId={} reason={}",
                event.getReservationId(), event.getPaymentOrderId(), event.getReason());

        reservationRepository.findById(event.getReservationId()).ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.FAILED);
            reservationRepository.save(reservation);

            // Release stalls back to AVAILABLE so others can book
            UpdateStallStatusRequest statusRequest = new UpdateStallStatusRequest();
            statusRequest.setStallIds(reservation.getStallIds());
            statusRequest.setBookingStatus("AVAILABLE");
            try {
                exhibitionServiceClient.updateBookingStatus(statusRequest);
            } catch (Exception ex) {
                log.warn("Failed to release stalls for reservationId={}: {}", reservation.getId(), ex.getMessage());
            }
        });
    }
}
