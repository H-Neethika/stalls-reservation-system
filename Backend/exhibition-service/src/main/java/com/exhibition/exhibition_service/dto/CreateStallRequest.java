package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreateStallRequest {
    private List<PointDto> points;
    private String path;
    private Long stallTypeId;
    private Long hallId;
}
