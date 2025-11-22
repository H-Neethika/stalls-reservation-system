package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class StallSummaryResponse {
    private Long id;
    private Long price;
    private String stallType;
    private String hallName;
    private String bookingStatus;
}
