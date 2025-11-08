package com.booking.booking_service.controller;

import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.request.CreateExhibitionHallRequest;
import com.booking.booking_service.response.ExhibitionHallResponse;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.service.ExhibitionHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibition-halls")
@CrossOrigin(origins = "*")
public class ExhibitionHallController {

    @Autowired
    private ExhibitionHallService exhibitionHallService;

    @PostMapping
    public ResponseEntity<ExhibitionHall> createExhibitionHall(@RequestBody CreateExhibitionHallRequest exhibitionHall) {
        ExhibitionHall saved = exhibitionHallService.createExhibitionHall(exhibitionHall);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ExhibitionHallResponse>> getAllExhibitionHalls() {
        return ResponseEntity.ok(exhibitionHallService.getAllExhibitionHalls());
    }

    // ✅ Get by ID (returns single DTO)
    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionHallResponse> getExhibitionHallById(@PathVariable Long id) {
        return exhibitionHallService.getExhibitionHallById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExhibitionHall> updateExhibitionHall(
            @PathVariable Long id,
            @RequestBody CreateExhibitionHallRequest updatedExhibitionHall) {
        ExhibitionHall updated = exhibitionHallService.updateExhibitionHall(id, updatedExhibitionHall);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteExhibitionHall(@PathVariable Long id) {
        exhibitionHallService.deleteExhibitionHall(id);
        MessageResponse messageResponse=new MessageResponse();
        messageResponse.setMessage("Exhibition hallId "+id+" deleted successfully!");
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }
}
