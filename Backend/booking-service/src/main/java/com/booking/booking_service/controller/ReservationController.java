package com.booking.booking_service.controller;

import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.request.CreatePaymentRequest;
import com.booking.booking_service.request.ReservationRequest;
import com.booking.booking_service.response.PaymentIntentResponse;
import com.booking.booking_service.response.ReservationResponse;
import com.booking.booking_service.service.ReservationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reservation")
public class ReservationController {

  @Autowired
  private ReservationService reservationService;

  @PostMapping
  public ResponseEntity<Reservation> createReservation(
      @RequestBody ReservationRequest reservationRequest) {
    Reservation reservation = reservationService.createReservation(reservationRequest);
    return new ResponseEntity<>(reservation, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) throws Exception {
    ReservationResponse reservation = reservationService.getReservationById(id);
    return new ResponseEntity<>(reservation, HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<List<ReservationResponse>> getAllReservation() {
    List<ReservationResponse> reservationList = reservationService.getAllReservation();
    return new ResponseEntity<>(reservationList, HttpStatus.OK);
  }

  @PutMapping("/payment")
  public ResponseEntity<PaymentIntentResponse> updateReservation(
      @RequestBody CreatePaymentRequest request) {
    PaymentIntentResponse response = reservationService.updateReservation(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }




}
