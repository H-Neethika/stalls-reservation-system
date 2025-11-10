package com.booking.booking_service.request;


import com.booking.booking_service.model.Hall;
import lombok.Data;

import java.util.List;

@Data
public class CreateExhibitionHallRequest {

    private Long hallId;
    private Long exhibitionId;
    private Long rows;
    private Long columns;

    private List<StallTypeRequest> stallTypes;


}
