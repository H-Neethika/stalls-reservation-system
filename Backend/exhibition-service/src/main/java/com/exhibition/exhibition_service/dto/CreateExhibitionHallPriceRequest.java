package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class CreateExhibitionHallPriceRequest {
    private Long exhibitionHallId;
    private Long stallTypeId;
    private Long price;
}
