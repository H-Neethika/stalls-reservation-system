package com.booking.booking_service.service;

import com.booking.booking_service.dto.response.PaymentSuccessResponse;

public interface ExhibitionStallService {

  PaymentSuccessResponse updateStallBookingStatus(Long reservationId);
}
