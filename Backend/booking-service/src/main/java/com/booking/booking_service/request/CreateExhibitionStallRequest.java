package com.booking.booking_service.request;


import com.booking.booking_service.model.StallType;
import lombok.Data;

import java.util.List;

@Data
public class CreateExhibitionStallRequest {

    private Long exhibitionHallId;

    private String stallName;

    private Long price;

    private Long rowPosition;

    private Long columnPosition;

    private Long stallTypeId;


}
