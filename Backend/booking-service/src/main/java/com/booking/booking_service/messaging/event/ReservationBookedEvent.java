package com.booking.booking_service.messaging.event;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationBookedEvent {
    private Long reservationId;
    private Long userId;
    private String userName;
    private String email;
    private String fairName;
    private String notificationType;
    private Instant bookingTime;
    private Instant eventStartTime;
    private Instant eventEndTime;
    private String eventLink;
    private List<StallSummary> stalls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StallSummary {
        private String stallName;
        private String stallType;
        private String hallName;
    }
}
