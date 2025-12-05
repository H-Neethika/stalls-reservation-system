package com.notification.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class StallInfo {
    private String stallName;
    private String stallType;
    private String hallName; // include this because many fairs have multi-hall bookings
}
