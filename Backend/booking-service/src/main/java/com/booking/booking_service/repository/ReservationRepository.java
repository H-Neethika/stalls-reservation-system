package com.booking.booking_service.repository;

import com.booking.booking_service.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.booking.booking_service.enums.ReservationStatus;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, Date createdBefore);
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
