package com.booking.booking_service.request;

import com.booking.booking_service.dto.HallPriceDto;
import lombok.Data;

import java.util.List;

@Data
public class CreateExhibitionStallRequest {
    private Long exhibitionId;
    private List<HallPriceDto> hallPriceList;

}
