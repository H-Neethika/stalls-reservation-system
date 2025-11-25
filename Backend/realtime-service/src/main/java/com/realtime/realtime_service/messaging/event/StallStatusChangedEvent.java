package com.realtime.realtime_service.messaging.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StallStatusChangedEvent {
    private Long exhibitionId;
    private Long hallId;
    private Long stallId;
    private Long exhibitionStallId;
    private String status;
    private Long reservationId;
}
