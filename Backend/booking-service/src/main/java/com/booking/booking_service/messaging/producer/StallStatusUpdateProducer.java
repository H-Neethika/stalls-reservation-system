package com.booking.booking_service.messaging.producer;

import com.booking.booking_service.messaging.event.StallStatusUpdateEvent;
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
public class StallStatusUpdateProducer {

    private final KafkaTemplate<String, StallStatusUpdateEvent> kafkaTemplate;

    @Value("${app.kafka.topics.stall-status-update:stall.status.update}")
    private String stallStatusTopic;

    public void publishReservedStatus(Long exhibitionId,Long reservationId, java.util.List<Long> stallIds) {
        if (reservationId == null|| exhibitionId == null ||  stallIds == null || stallIds.isEmpty()) {
            log.warn("Skipping stall status publish - missing reservationId or stallIds");
            return;
        }

        StallStatusUpdateEvent event = StallStatusUpdateEvent.builder()
            .exhibitionId(exhibitionId)
                .reservationId(reservationId)
                .stallIds(stallIds)
                .bookingStatus("RESERVED")
                .build();

        CompletableFuture<SendResult<String, StallStatusUpdateEvent>> future =
                kafkaTemplate.send(stallStatusTopic, String.valueOf(reservationId), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish stall status update for reservationId={}", reservationId, ex);
            } else {
                log.info("Published stall status update for reservationId={} on topic={}",
                        reservationId, stallStatusTopic);
            }
        });
    }
}
