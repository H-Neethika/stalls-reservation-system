package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.dto.StallStatusResponse;
import com.exhibition.exhibition_service.dto.UpdateStallStatusRequest;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking-status")
@RequiredArgsConstructor
public class BookingStatusController {

    private final LayoutService layoutService;

    @PostMapping("/update")
    public ResponseEntity<List<StallStatusResponse>> updateStatuses(@RequestBody UpdateStallStatusRequest request) {
        return ResponseEntity.ok(layoutService.updateStallStatuses(request));
    }
}
