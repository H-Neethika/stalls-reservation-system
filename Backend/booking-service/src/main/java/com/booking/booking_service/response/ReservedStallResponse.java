package com.booking.booking_service.response;

import lombok.Data;

@Data
public class ReservedStallResponse {
    private Long id;
    private String stallName;
    private Long price;
    private String stallType;
    private String hallName;
    private String bookingStatus;
}
