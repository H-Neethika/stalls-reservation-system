package com.booking.booking_service.service;

import com.booking.booking_service.dto.response.ExhibitionDTO;
import com.booking.booking_service.dto.response.ExternalStallSummaryResponse;
import com.booking.booking_service.dto.request.UpdateStallStatusRequest;
import com.booking.booking_service.dto.response.StallStatusResponse;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/api/layout/exhibitions/{exhibitionId}/stalls/summary")
    List<ExternalStallSummaryResponse> getExhibitionStallSummaries(
        @PathVariable Long exhibitionId,
        @RequestParam("ids") List<Long> ids
    );

    @GetMapping("/api/exhibition/{id}")
    public ResponseEntity<ExhibitionDTO> getExhibition(@PathVariable Long id);


}
