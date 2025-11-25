package com.exhibition.exhibition_service.messaging.event;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StallStatusUpdateEvent {
    private Long reservationId;
    private List<Long> stallIds;
    private String bookingStatus;
}
