package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class CreateExhibitionHallRequest {
    private Long hallId;
    private Long exhibitionId;
}
