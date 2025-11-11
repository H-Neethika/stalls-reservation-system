package com.notification.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetails {
    private String fairName;
    private String stallName;
    private LocalDateTime bookingTime;
    private LocalDateTime eventTime;
    private String userName;
    private URI eventLink;
}
