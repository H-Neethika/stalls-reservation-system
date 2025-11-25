package com.notification.notification_service.dto;

import com.notification.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String stallName;
    private String stallType;
    private String hallName;
    private LocalDateTime bookingTime;
    private LocalDateTime eventTime;
    private String eventLink;
}
