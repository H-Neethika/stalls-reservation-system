package com.booking.booking_service.dto.response;

import lombok.Data;

@Data
public class ReservedStallResponse {
    private Long id;
    private Long price;
    private String stallType;
    private String hallName;
    private String stallName;
    private String bookingStatus;
}
