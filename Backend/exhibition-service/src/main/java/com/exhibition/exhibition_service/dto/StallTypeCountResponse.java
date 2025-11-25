package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class StallTypeCountResponse {
    private Long stallTypeId;
    private String stallType;
    private long count;
}
