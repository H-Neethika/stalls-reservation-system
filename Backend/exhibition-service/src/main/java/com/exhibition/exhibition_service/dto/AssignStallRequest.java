package com.exhibition.exhibition_service.dto;

import lombok.Data;

@Data
public class AssignStallRequest {
    private Long stallId;
    private Long exhibitionId;
}
