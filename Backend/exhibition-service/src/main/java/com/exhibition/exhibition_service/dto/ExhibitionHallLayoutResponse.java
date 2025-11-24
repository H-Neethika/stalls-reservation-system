package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class ExhibitionHallLayoutResponse {
    private Long hallId;
    private Long exhibitionHallId;
    private String hallName;
    private List<StallSimpleLayoutResponse> stalls;
}
