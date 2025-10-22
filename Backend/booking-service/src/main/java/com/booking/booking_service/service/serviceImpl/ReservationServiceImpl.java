package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.repository.ExhibitionStallRepository;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.request.ReservationRequest;
import com.booking.booking_service.service.ReservationService;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservationServiceImpl implements ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private ExhibitionStallRepository exhibitionStallRepository;


  @Override
  public Reservation createReservation(ReservationRequest reservationRequest) {
    List<ExhibitionStall> stalls = exhibitionStallRepository.findAllById(
        reservationRequest.getStallIds());

    Long totalAmount = 0L;
    for (ExhibitionStall stall : stalls) {
      totalAmount += stall.getPrice();
    }
    Reservation newReservation = new Reservation();
    newReservation.setUserId(reservationRequest.getUserId());
    newReservation.setStall(stalls);
    newReservation.setTotalAmount(totalAmount);
    newReservation.setCreatedAt(new Date());

    return reservationRepository.save(newReservation);
  }


  @Override
  public Reservation getReservationById(Long id) throws Exception {
    Optional<Reservation> reservation = reservationRepository.findById(id);
    if (reservation.isEmpty()) {
      throw new Exception("Reservation not found with id " + id);
    }
    return reservation.get();
  }

  @Override
  public List<Reservation> getAllReservation() {
    List<Reservation> reservationList = reservationRepository.findAll();
    return reservationList;
  }
}
