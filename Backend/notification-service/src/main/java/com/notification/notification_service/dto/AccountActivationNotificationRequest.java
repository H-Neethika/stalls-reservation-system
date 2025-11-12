package com.notification.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountActivationNotificationRequest {
    private Long userId;
    private String email;
    private String userName;
    private LocalDateTime createdTime;
    private String role;
    private URI loginLink;
}
