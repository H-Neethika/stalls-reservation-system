package com.booking.booking_service.controller;

import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.dto.request.CreatePaymentRequest;
import com.booking.booking_service.dto.request.ReservationRequest;
import com.booking.booking_service.dto.response.PaymentIntentResponse;
import com.booking.booking_service.dto.response.ReservationResponse;
import com.booking.booking_service.service.ReservationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
      @RequestBody ReservationRequest reservationRequest,
      JwtAuthenticationToken authentication) {
    Long userId = extractUserId(authentication);
    Reservation reservation = reservationService.createReservation(reservationRequest, userId);
    return new ResponseEntity<>(reservation, HttpStatus.CREATED);
  }

  @GetMapping("/my")
  public ResponseEntity<List<ReservationResponse>> getReservationsForCurrentUser(
      JwtAuthenticationToken authentication) {
    Long userId = extractUserId(authentication);
    List<ReservationResponse> reservations = reservationService.getReservationsForUser(userId);
    return new ResponseEntity<>(reservations, HttpStatus.OK);
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

  private Long extractUserId(JwtAuthenticationToken authentication) {
    if (authentication == null || authentication.getToken() == null) {
      throw new IllegalArgumentException("Authentication token is required");
    }
    // Prefer explicit userId claim if present
    Object userIdClaim = authentication.getToken().getClaim("userId");
    if (userIdClaim != null) {
      try {
        return Long.valueOf(userIdClaim.toString());
      } catch (NumberFormatException ignored) {
        // fall through to sub
      }
    }

    Object sub = authentication.getToken().getClaim("sub");
    if (sub == null) {
      throw new IllegalArgumentException("Token missing subject");
    }
    try {
      return Long.valueOf(sub.toString());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Subject claim is not a valid user id");
    }
  }
}
