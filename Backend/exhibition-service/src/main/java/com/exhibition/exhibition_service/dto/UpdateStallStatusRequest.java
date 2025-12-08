package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateStallStatusRequest {
    private Long exhibitionId;
    private List<Long> stallIds;
    private String bookingStatus;
}
