package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class HallSummaryResponse {
    private Long id;
    private String hallName;
    private long totalStalls;
    private List<StallTypeCountResponse> stallTypes;
}
