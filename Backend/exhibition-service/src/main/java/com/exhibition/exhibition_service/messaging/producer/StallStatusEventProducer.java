package com.exhibition.exhibition_service.messaging.producer;

import com.exhibition.exhibition_service.messaging.event.StallStatusChangedEvent;
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
public class StallStatusEventProducer {

    private final KafkaTemplate<String, StallStatusChangedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.stall-status-events:stall.status.events}")
    private String stallStatusTopic;

    public void publish(StallStatusChangedEvent event) {
        if (event == null) {
            return;
        }
        CompletableFuture<SendResult<String, StallStatusChangedEvent>> future =
                kafkaTemplate.send(stallStatusTopic, String.valueOf(event.getExhibitionId()), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish stall status event for stallId={} exhibitionId={}",
                        event.getStallId(), event.getExhibitionId(), ex);
            } else {
                log.debug("Published stall status event for stallId={} exhibitionId={} topic={}",
                        event.getStallId(), event.getExhibitionId(), stallStatusTopic);
            }
        });
    }
}
