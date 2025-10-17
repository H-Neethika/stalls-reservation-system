package com.booking.booking_service.repository;

import com.booking.booking_service.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStatusRepository extends JpaRepository<BookingStatus,Long> {
}
