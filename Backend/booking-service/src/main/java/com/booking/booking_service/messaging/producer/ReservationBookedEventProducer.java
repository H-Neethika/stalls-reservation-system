package com.booking.booking_service.messaging.producer;

import com.booking.booking_service.messaging.event.ReservationBookedEvent;
import com.booking.booking_service.response.PaymentSuccessResponse;
import com.booking.booking_service.dto.StallDto;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationBookedEventProducer {

    private final KafkaTemplate<String, ReservationBookedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.reservation-booked:reservation.booked}")
    private String reservationBookedTopic;

    @Value("${app.notification.default-event-link:https://example.com/events}")
    private String defaultEventLink;

    @Value("${app.notification.default-fair-name:Stall Reservation}")
    private String defaultFairName;

    public void publishReservationBooked(PaymentSuccessResponse response) {
        if (response == null) {
            log.warn("Skipping reservation booked event publish - response is null");
            return;
        }

        String fairName = Optional.ofNullable(response.getFairName())
                .filter(name -> !name.isBlank())
                .orElse(defaultFairName);

        Instant bookingInstant = Optional.ofNullable(response.getBookingDateTime())
                .map(date -> date.toInstant())
                .orElse(Instant.now());

        List<ReservationBookedEvent.StallSummary> stalls = Optional.ofNullable(response.getStalls())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        ReservationBookedEvent event = ReservationBookedEvent.builder()
                .reservationId(response.getReservationId())
                .userId(response.getUserId())
                .userName(response.getUsername())
                .email(response.getEmail())
                .fairName(fairName)
                .notificationType("STALL_RESERVATION")
                .bookingTime(bookingInstant)
                .eventTime(bookingInstant)
                .eventLink(defaultEventLink)
                .stalls(stalls)
                .build();

        CompletableFuture<SendResult<String, ReservationBookedEvent>> future =
                kafkaTemplate.send(reservationBookedTopic, String.valueOf(event.getReservationId()), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish reservation booked event for reservationId={}", event.getReservationId(), ex);
            } else {
                log.info("Reservation booked event dispatched for reservationId={} topic={}",
                        event.getReservationId(), reservationBookedTopic);
            }
        });
    }

    private ReservationBookedEvent.StallSummary mapToSummary(StallDto stallDto) {
        return ReservationBookedEvent.StallSummary.builder()
                .stallName(stallDto.getStallName())
                .stallSize(stallDto.getStallSize())
                .build();
    }
}
