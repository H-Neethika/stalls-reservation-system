package com.booking.booking_service.service;

import com.booking.booking_service.dto.response.ExternalStallSummaryResponse;
import com.booking.booking_service.dto.request.UpdateStallStatusRequest;
import com.booking.booking_service.dto.response.StallStatusResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "EXHIBITION-SERVICE")
public interface ExhibitionServiceClient {

  @GetMapping("/api/layout/stalls/summary")
  List<ExternalStallSummaryResponse> getStallSummaries(@RequestParam("ids") List<Long> ids);

  @PostMapping("/api/booking-status/update")
  List<StallStatusResponse> updateBookingStatus(@RequestBody UpdateStallStatusRequest request);
}
