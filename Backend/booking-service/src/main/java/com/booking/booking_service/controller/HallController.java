package com.booking.booking_service.controller;

import com.booking.booking_service.model.Hall;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.service.HallService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hall")
public class HallController {

    @Autowired
    private HallService hallService;

    @GetMapping
    public ResponseEntity<List<Hall>> getAllHalls() {
        List<Hall> hallList = hallService.getAllHalls();
        return new ResponseEntity<>(hallList, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Hall> createHall(@RequestBody Hall hall) {
        Hall createdHall = hallService.createHall(hall);
        return new ResponseEntity<>(createdHall, HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Hall> updateHall(@PathVariable Long id, @RequestBody Hall hall) {
        Hall updatedHall = hallService.updateHall(id, hall);
        return new ResponseEntity<>(updatedHall, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        MessageResponse messageResponse=new MessageResponse();
        messageResponse.setMessage("Hall Id "+id+" deleted successfully!");
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);

    }

}
