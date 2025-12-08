package com.booking.booking_service.messaging.event;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Kafka payload used to drive stall status changes inside exhibition-service after payment success.
 */
@Value
@Builder
public class StallStatusUpdateEvent {
    Long reservationId;
    List<Long> stallIds;
    String bookingStatus; // e.g. RESERVED
    Long exhibitionId;
}
