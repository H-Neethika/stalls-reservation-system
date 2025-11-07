package com.booking.booking_service.response;

import lombok.Data;
import java.util.List;

@Data
public class ExhibitionHallResponse {
    private Long id;
    private Long exhibitionId;
    private Long hallId;
    private String hallName;
    private Long rows;
    private Long columns;

    private List<StallTypeResponse> stallTypes;
}
