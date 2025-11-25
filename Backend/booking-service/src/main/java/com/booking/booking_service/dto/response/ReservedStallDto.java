package com.booking.booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservedStallDto {

  private String stallName;
  private String stallType;
  private String hallName;

}
