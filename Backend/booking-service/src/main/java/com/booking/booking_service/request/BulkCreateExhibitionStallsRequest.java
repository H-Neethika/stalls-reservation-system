package com.booking.booking_service.request;

import lombok.Data;
import java.util.List;

@Data
public class BulkCreateExhibitionStallsRequest {

    private Long exhibitionHallId;
    private List<CreateExhibitionStallRequest> stalls;
}
