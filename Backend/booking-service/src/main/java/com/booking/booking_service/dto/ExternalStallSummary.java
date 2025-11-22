package com.booking.booking_service.dto;

import lombok.Data;

@Data
public class ExternalStallSummary {
    private Long id;
    private Long price;
    private String stallType;
    private String hallName;
}
