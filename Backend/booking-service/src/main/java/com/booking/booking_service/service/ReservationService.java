package com.booking.booking_service.service;

import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.dto.request.ReservationRequest;
import com.booking.booking_service.dto.request.CreatePaymentRequest;
import com.booking.booking_service.dto.response.PaymentIntentResponse;
import java.util.List;
import com.booking.booking_service.dto.response.ReservationResponse;
import org.springframework.stereotype.Service;

@Service
public interface ReservationService {

  Reservation createReservation(ReservationRequest reservationRequest, Long userId);

  ReservationResponse getReservationById(Long id) throws Exception;

  List<ReservationResponse> getAllReservation();

  PaymentIntentResponse updateReservation(CreatePaymentRequest request);


}
