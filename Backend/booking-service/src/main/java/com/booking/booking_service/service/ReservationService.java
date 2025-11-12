package com.booking.booking_service.service;

import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.request.ReservationRequest;
import com.booking.booking_service.request.CreatePaymentRequest;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.response.PaymentIntentResponse;
import java.util.List;

import com.booking.booking_service.response.ReservationResponse;
import org.springframework.stereotype.Service;

@Service
public interface ReservationService {

  public Reservation createReservation(ReservationRequest reservationRequest);

  ReservationResponse getReservationById(Long id) throws Exception;

  List<ReservationResponse> getAllReservation();

  public PaymentIntentResponse updateReservation(CreatePaymentRequest request);


}
