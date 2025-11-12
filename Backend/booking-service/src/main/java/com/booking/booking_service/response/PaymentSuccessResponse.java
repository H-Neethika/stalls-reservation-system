package com.booking.booking_service.response;

import com.booking.booking_service.dto.StallDto;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessResponse {

  private Long reservationId;
  private Long userId;
  private String username;
  private String email;
  private List<StallDto> stalls;
  private Date bookingDateTime;

}
