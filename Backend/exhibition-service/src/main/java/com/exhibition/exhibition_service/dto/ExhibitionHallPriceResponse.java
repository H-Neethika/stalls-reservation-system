package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class ExhibitionHallPriceResponse {
    private Long id;
    private Long exhibitionHallId;
    private String hallName;
    private Long stallTypeId;
    private String stallType;
    private Long price;
}
