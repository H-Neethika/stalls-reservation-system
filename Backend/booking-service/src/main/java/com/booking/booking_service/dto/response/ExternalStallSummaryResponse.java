package com.booking.booking_service.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ExternalStallSummaryResponse {
    private Long id;
    private String displayName;
    private Long price;
    private String stallType;
    private String hallName;
    private String bookingStatus;
    private String stallName;
    private List<String> genres;
}
