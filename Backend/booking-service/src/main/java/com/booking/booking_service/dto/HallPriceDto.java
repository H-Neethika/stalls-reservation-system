package com.booking.booking_service.dto;


import lombok.Data;

import java.util.List;

@Data
public class HallPriceDto {

    private Long hallId;

    private List<PriceDto> priceList;
}
