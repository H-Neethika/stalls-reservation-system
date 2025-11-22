package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.BookingStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStatusRepository extends JpaRepository<BookingStatus, Long> {
    Optional<BookingStatus> findByStatus(String status);
}
