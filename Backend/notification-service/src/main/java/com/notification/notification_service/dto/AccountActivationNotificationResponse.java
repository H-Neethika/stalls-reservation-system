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
public class AccountActivationNotificationResponse {
    private Long userId;
    private String recipientEmail;
    private LocalDateTime createdTime;
    private NotificationType notificationType;
    private String status;

}
