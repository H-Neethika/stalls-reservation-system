package com.notification.notification_service.dto;

import lombok.Data;

import java.net.URI;
import java.time.LocalDateTime;

@Data
public class BookingEvent {
    private String fairName;
    private String stallName;
    private LocalDateTime bookingTime;
    private LocalDateTime eventTime;
    private Long reservationId;
    private String userEmail;
    private String userName;
    private URI eventLink;
}
