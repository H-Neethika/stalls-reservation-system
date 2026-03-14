package com.booking.booking_service.dto.response;

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
  private String fairName;
  private List<ReservedStallDto> stalls;
  private Date bookingDateTime;
  private Date eventStartDateTime;
  private Date eventEndDateTime;

}
