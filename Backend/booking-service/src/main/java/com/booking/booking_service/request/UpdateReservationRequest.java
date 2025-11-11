package com.booking.booking_service.request;

import com.booking.booking_service.response.CreatePaymentRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReservationRequest {
  private CreatePaymentRequest updateRequest;
  private List<Long> stallIds;

}
