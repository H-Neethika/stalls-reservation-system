package com.notification.notification_service.messaging.event;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
    private Instant eventTime;
    private String eventLink;
    private List<StallSummary> stalls;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StallSummary {
        private String stallName;
        private String stallType;
        private String hallName;
    }
}
