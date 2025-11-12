package com.booking.booking_service.controller;


import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.request.BulkCreateExhibitionStallsRequest;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import com.booking.booking_service.response.ExhibitionStallResponse;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.response.PaymentSuccessResponse;
import com.booking.booking_service.service.ExhibitionStallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibition-stalls")
public class ExhibitionStallController {


    @Autowired
    private ExhibitionStallService exhibitionStallService;

    @PostMapping
    public ResponseEntity<ExhibitionStall> createExhibitionStall(@RequestBody CreateExhibitionStallRequest request) {
        ExhibitionStall created = exhibitionStallService.createExhibitionStall(request);
        return ResponseEntity.ok(created);
    }


    @PostMapping("/bulk")
    public ResponseEntity<List<ExhibitionStall>> createMultipleStalls(
            @RequestBody BulkCreateExhibitionStallsRequest request) {
        List<ExhibitionStall> created = exhibitionStallService.createMultipleExhibitionStalls(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ExhibitionStallResponse>> getAllExhibitionStalls() {
        return ResponseEntity.ok(exhibitionStallService.getAllExhibitionStalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionStallResponse> getExhibitionStallById(@PathVariable Long id) {
        return exhibitionStallService.getExhibitionStallById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExhibitionStall> updateExhibitionStall(
            @PathVariable Long id,
            @RequestBody CreateExhibitionStallRequest request) {
        ExhibitionStall updated = exhibitionStallService.updateExhibitionStall(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteExhibitionStall(@PathVariable Long id) {
        exhibitionStallService.deleteExhibitionStall(id);
        MessageResponse messageResponse=new MessageResponse();
        messageResponse.setMessage("Exhibition stallId "+id+" deleted successfully!");
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);

    }

    @PutMapping("/booking-status/{id}")
    public ResponseEntity<PaymentSuccessResponse> updateExhibitionStall(
        @PathVariable Long id) {
        PaymentSuccessResponse response = exhibitionStallService.updateStallBookingStatus(id);
        return ResponseEntity.ok(response);
    }

}
