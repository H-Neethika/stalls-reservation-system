package com.exhibition.exhibition_service.messaging.listener;

import com.exhibition.exhibition_service.dto.UpdateStallStatusRequest;
import com.exhibition.exhibition_service.messaging.event.StallStatusUpdateEvent;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StallStatusUpdateListener {

    private final LayoutService layoutService;

    @KafkaListener(
            topics = "${app.kafka.topics.stall-status-update:stall.status.update}",
            containerFactory = "stallStatusKafkaListenerContainerFactory"
    )
    public void handleStallStatusUpdate(@Payload StallStatusUpdateEvent event) {
        if (event == null) {
            log.warn("Received null stall status update event");
            return;
        }
        List<Long> stallIds = event.getStallIds();
        if (stallIds == null || stallIds.isEmpty()) {
            log.warn("Stall status update event missing stallIds for reservationId={}", event.getReservationId());
            return;
        }
        String bookingStatus = event.getBookingStatus() != null ? event.getBookingStatus() : "RESERVED";
        log.info("Applying stall status update: reservationId={}, stalls={}, status={}",
                event.getReservationId(), stallIds, bookingStatus);

        try {
            UpdateStallStatusRequest request = new UpdateStallStatusRequest();
            request.setStallIds(stallIds);
            request.setBookingStatus(bookingStatus);
            layoutService.updateStallStatuses(request);
        } catch (Exception ex) {
            log.error("Failed to apply stall status update for reservationId={} stalls={}",
                    event.getReservationId(), stallIds, ex);
            throw ex;
        }
    }
}
