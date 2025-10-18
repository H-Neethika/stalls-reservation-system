package com.booking.booking_service.controller;


import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Genre;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import com.booking.booking_service.service.ExhibitionStallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibition-stall")
public class ExhibitionStallController {

    @Autowired
    private ExhibitionStallService exhibitionStallService;

    @PostMapping()
    public ResponseEntity<List<ExhibitionStall>> createExhibitionStalls(@RequestBody CreateExhibitionStallRequest exhibitionStallReq) {
        List<ExhibitionStall> createdStalls = exhibitionStallService.createExhibitionStall(exhibitionStallReq);
        return ResponseEntity.ok(createdStalls);
    }

    @GetMapping
    public ResponseEntity<Page<ExhibitionStall>> getExhibitionStalls(
            @RequestParam(required = false) Long hallId,
            @RequestParam(required = false) String bookingStatus,
            @RequestParam(required = false) String stallType,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Long exhibitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExhibitionStall> stalls = exhibitionStallService.getExhibitionStall(hallId, bookingStatus, stallType, genre, exhibitionId, pageable);

        return ResponseEntity.ok(stalls);
    }

    @PutMapping()
    public ResponseEntity<ExhibitionStall> updateExhibitionStall(@RequestParam Long exhibitionId, @RequestParam Long stallId, @RequestBody ExhibitionStall updateStall) {
        ExhibitionStall updatedExhibitionStall = exhibitionStallService.updateExhibitionStall(stallId, exhibitionId, updateStall);
        return ResponseEntity.ok(updatedExhibitionStall);
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteExhibitionStall(@RequestParam Long exhibitionId, @RequestParam Long stallId) {
        exhibitionStallService.deleteExhibitionStall(stallId, exhibitionId);
        return ResponseEntity.ok("ExhibitionStall deleted successfully for exhibitionId: " + exhibitionId + " & stallId: " + stallId);
    }

    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getExhibitionStallGenres(@RequestParam(required = false) Long hallId,@RequestParam(required = false) Long stallId) {
        List<Genre> genres=exhibitionStallService.getExhibitionStallGenres(hallId,stallId);
        return ResponseEntity.ok(genres);

    }


}
