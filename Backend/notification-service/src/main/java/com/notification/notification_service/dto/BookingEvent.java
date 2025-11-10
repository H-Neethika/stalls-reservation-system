package com.notification.notification_service.dto;

import lombok.Data;

@Data
public class BookingEvent {
    private Long reservationId;
    private String userEmail;
    private String userName;
}
