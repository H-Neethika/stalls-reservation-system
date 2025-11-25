package com.realtime.realtime_service.messaging.listener;

import com.realtime.realtime_service.messaging.event.StallStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StallStatusEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "${app.kafka.topics.stall-status-events:stall.status.events}",
            containerFactory = "stallStatusKafkaListenerContainerFactory"
    )
    public void handleStallStatus(@Payload StallStatusChangedEvent event) {
        if (event == null || event.getExhibitionId() == null || event.getStallId() == null) {
            log.warn("Received invalid stall status event: {}", event);
            return;
        }
        String destination = "/topic/exhibition/" + event.getExhibitionId() + "/stalls";
        messagingTemplate.convertAndSend(destination, event);
    }
}
