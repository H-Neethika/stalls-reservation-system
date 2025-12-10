package com.notification.notification_service.dto;

import com.notification.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationNotificationRequest {
    private Long userId;
    private String userName;
    private NotificationType notificationType;
    private String email;
    private Long reservationId;
    private String fairName;
    private String displayName;
    private List<StallInfo> stalls;
    private LocalDateTime bookingTime;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private URI eventLink;
}
