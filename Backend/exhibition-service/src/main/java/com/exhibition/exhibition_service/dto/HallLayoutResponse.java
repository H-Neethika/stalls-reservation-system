package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class HallLayoutResponse {
    private Long hallId;
    private String hallName;
    private List<StallLayoutResponse> stalls;
}
