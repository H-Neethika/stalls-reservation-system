package com.notification.notification_service.messaging.listener;

import com.notification.notification_service.dto.ReservationNotificationRequest;
import com.notification.notification_service.enums.NotificationType;
import com.notification.notification_service.messaging.event.ReservationBookedEvent;
import com.notification.notification_service.messaging.event.ReservationBookedEvent.StallSummary;
import com.notification.notification_service.service.NotificationService;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationBookedListener {

    private final NotificationService notificationService;

    @Value("${OFFICIAL_WEBSITE_LINK:https://example.com}")
    private String defaultWebsiteLink;

    @KafkaListener(topics = "${app.kafka.topics.reservation-booked:reservation.booked}",
            containerFactory = "reservationBookedKafkaListenerContainerFactory")
    public void handleReservationBooked(@Payload ReservationBookedEvent event) {
        if (event == null) {
            log.warn("Received null reservation booked event");
            return;
        }

        log.info("Triggering reservation notification for reservationId={} userId={}",
                event.getReservationId(), event.getUserId());

        ReservationNotificationRequest request = buildRequest(event);
        notificationService.sendReservationConfirmationEmail(request);
    }

    private ReservationNotificationRequest buildRequest(ReservationBookedEvent event) {
        ReservationNotificationRequest request = new ReservationNotificationRequest();
        request.setReservationId(event.getReservationId());
        request.setUserId(event.getUserId());
        request.setUserName(event.getUserName());
        request.setEmail(event.getEmail());
        request.setFairName(event.getFairName());
        request.setNotificationType(resolveType(event.getNotificationType()));
        request.setBookingTime(toLocalDateTime(event.getBookingTime()));
        request.setEventTime(toLocalDateTime(event.getEventTime()));
        request.setEventLink(resolveEventLink(event.getEventLink()));

        StallSummary stall = firstStall(event.getStalls());
        if (stall != null) {
            request.setStallName(stall.getStallName());
            request.setStallSize(stall.getStallSize());
        }
        return request;
    }

    private StallSummary firstStall(List<StallSummary> stalls) {
        if (stalls == null || stalls.isEmpty()) {
            return null;
        }
        return stalls.get(0);
    }

    private NotificationType resolveType(String type) {
        try {
            return type == null ? NotificationType.STALL_RESERVATION : NotificationType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown notification type {}. Falling back to STALL_RESERVATION", type);
            return NotificationType.STALL_RESERVATION;
        }
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? LocalDateTime.now(ZoneOffset.UTC)
                : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private URI resolveEventLink(String eventLink) {
        try {
            return eventLink == null ? URI.create(defaultWebsiteLink) : URI.create(eventLink);
        } catch (Exception ex) {
            log.warn("Invalid event link {}. Using default {}", eventLink, defaultWebsiteLink);
            return URI.create(defaultWebsiteLink);
        }
    }
}
