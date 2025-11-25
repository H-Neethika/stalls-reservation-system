package com.exhibition.exhibition_service.messaging.event;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StallStatusChangedEvent {
    Long exhibitionId;
    Long hallId;
    Long stallId;
    Long exhibitionStallId;
    String status;
    Long reservationId;
}
