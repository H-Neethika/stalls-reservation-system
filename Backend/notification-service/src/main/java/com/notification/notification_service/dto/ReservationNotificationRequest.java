package com.notification.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationNotificationRequest {
    private Long userId;
    private String userName;
    private String email;
    private Long reservationId;
    private String fairName;
    private String stallName;
    private String stallSize;
    private LocalDateTime bookingTime;
    private LocalDateTime eventTime;
    private URI eventLink;
}
