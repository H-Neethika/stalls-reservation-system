package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class StallSimpleLayoutResponse {
    private Long stallId;
    private Long exhibitionStallId;
    private String status;
}
