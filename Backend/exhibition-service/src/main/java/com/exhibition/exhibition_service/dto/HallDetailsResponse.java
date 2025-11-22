package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class HallDetailsResponse {
    private Long hallId;
    private String hallName;
    private List<StallDetailsResponse> stalls;
}
