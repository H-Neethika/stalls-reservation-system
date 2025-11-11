package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.repository.BookingStatusRepository;
import com.booking.booking_service.repository.ExhibitionStallRepository;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.request.ReservationRequest;
import com.booking.booking_service.response.CreatePaymentRequest;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.response.PaymentIntentResponse;
import com.booking.booking_service.response.ReservationResponse;
import com.booking.booking_service.response.ReservedStallResponse;
import com.booking.booking_service.service.PaymentService;
import com.booking.booking_service.service.ReservationService;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationServiceImpl implements ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private ExhibitionStallRepository exhibitionStallRepository;
  @Autowired
  private PaymentService paymentService;
  @Autowired
  private BookingStatusRepository bookingStatusRepository;



  @Override
  public Reservation createReservation(ReservationRequest reservationRequest) {
    List<ExhibitionStall> stalls = exhibitionStallRepository.findAllById(
        reservationRequest.getStallIds());



    Long totalAmount = 0L;
    for (ExhibitionStall stall : stalls) {
      totalAmount += stall.getPrice();
      stall.setBookingStatus(bookingStatusRepository.findById(2L).get());
      exhibitionStallRepository.save(stall);
    }
    Reservation newReservation = new Reservation();
    newReservation.setUserId(reservationRequest.getUserId());
    newReservation.setStall(stalls);
    newReservation.setTotalAmount(totalAmount);
    newReservation.setCreatedAt(new Date());

    return reservationRepository.save(newReservation);
  }

  @Override
  public ReservationResponse getReservationById(Long id) throws Exception {
    Optional<Reservation> reservation = reservationRepository.findById(id);
    if (reservation.isEmpty()) {
      throw new Exception("Reservation not found with id " + id);
    }
    return mapToResponse(reservation.get());
  }

  @Override
  public List<ReservationResponse> getAllReservation() {
    return reservationRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public PaymentIntentResponse updateReservation(CreatePaymentRequest updateRequest,List<Long>stallIds) {



    List<ExhibitionStall> stalls = exhibitionStallRepository.findAllById(
        stallIds);

    for(ExhibitionStall stall: stalls){
      stall.setBookingStatus(bookingStatusRepository.findById(3L).get());
      exhibitionStallRepository.save(stall);
    }
    MessageResponse response = new MessageResponse();
    response.setMessage("Payment Successfully Completed");
    return paymentService.createPaymentIntent(updateRequest).getBody();
  }

  //Convert Entity to DTO
  private ReservationResponse mapToResponse(Reservation reservation) {
    ReservationResponse dto = new ReservationResponse();
    dto.setId(reservation.getId());
    dto.setUserId(reservation.getUserId());
    dto.setTotalAmount(reservation.getTotalAmount());
    dto.setCreatedAt(reservation.getCreatedAt());

    // Simplify stalls
    List<ReservedStallResponse> stalls = reservation.getStall()
            .stream()
            .map(st -> {
              ReservedStallResponse stallDto = new ReservedStallResponse();
              stallDto.setId(st.getId());
              stallDto.setStallName(st.getStallName());
              stallDto.setPrice(st.getPrice());
              stallDto.setStallType(st.getStallType() != null ? st.getStallType().getType() : null);
              stallDto.setHallName(st.getExhibitionHallId() != null ?
                      st.getExhibitionHallId().getHallId().getHallName() : null);
              stallDto.setBookingStatus(st.getBookingStatus() != null ?
                      st.getBookingStatus().getStatus() : null);
              return stallDto;
            })
            .collect(Collectors.toList());

    dto.setStalls(stalls);
    return dto;
  }
}
