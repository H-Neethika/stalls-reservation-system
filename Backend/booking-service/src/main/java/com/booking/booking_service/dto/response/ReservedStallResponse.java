package com.booking.booking_service.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ReservedStallResponse {
    private Long id;
    private String displayName;
    private Long price;
    private String stallType;
    private String hallName;
    private String stallName;
    private String bookingStatus;
    private List<String> genres;
}
