package com.notification.notification_service.model.email_details;

import com.notification.notification_service.dto.StallInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationEmailDetails extends EmailDetails {
    private Long reservationId;
    private String fairName;
    private String displayName;
    private List<StallInfo> stalls;
    private LocalDateTime bookingTime;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private URI eventLink;
}
