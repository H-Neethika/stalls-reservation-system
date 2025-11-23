package com.exhibition.exhibition_service.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ExhibitionBriefResponse {
    private Long exhibitionId;
    private String exhibitionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
