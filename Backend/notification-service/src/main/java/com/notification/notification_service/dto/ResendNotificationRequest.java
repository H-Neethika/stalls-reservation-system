package com.notification.notification_service.dto;

import com.notification.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResendNotificationRequest {
    private Long userId;
    private NotificationType notificationType;
    private Long reservationId;
}
