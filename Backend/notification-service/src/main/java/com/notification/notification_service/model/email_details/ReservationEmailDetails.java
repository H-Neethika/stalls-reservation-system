package com.notification.notification_service.model.email_details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationEmailDetails extends EmailDetails {
    private Long reservationId;
    private String fairName;
    private String stallName;
    private String stallType;
    private String hallName;
    private LocalDateTime bookingTime;
    private LocalDateTime eventTime;
    private URI eventLink;
}
