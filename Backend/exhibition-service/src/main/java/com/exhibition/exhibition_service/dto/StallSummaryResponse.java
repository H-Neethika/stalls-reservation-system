package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class StallSummaryResponse {
    private Long id;
    private String displayName;
    private Long price;
    private String stallType;
    private String hallName;
    private String bookingStatus;
    private String stallName;
    private List<String> genres;
}
