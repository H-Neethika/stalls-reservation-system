package com.booking.booking_service.repository;

import com.booking.booking_service.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingStatusRepository extends JpaRepository<BookingStatus,Long> {
    Optional<BookingStatus> findByStatus(String status);
}
