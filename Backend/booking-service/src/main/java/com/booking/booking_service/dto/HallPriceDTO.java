package com.booking.booking_service.dto;

import lombok.Data;

@Data
public class HallPriceDTO {
  private Long hallId;
  private Long stallTypeId;
  private Long price;
}
