package com.notification.notification_service.dto;

import com.notification.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationNotificationResponse {
    private Long userId;
    private String recipientEmail;
    private NotificationType notificationType;
    private String status;
    private String userName;
    private Long reservationId;
    private String fairName;
    private String displayName;
    private List<StallInfo> stalls;
    private LocalDateTime bookingTime;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private String eventLink;
}
