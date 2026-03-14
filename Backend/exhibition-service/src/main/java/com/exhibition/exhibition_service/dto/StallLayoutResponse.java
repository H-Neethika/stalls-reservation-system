package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class StallLayoutResponse {
    private Long id;
    private String displayName;
    private Long stallTypeId;
    private String stallType;
    private String path;
    private List<PointDto> points;
}
