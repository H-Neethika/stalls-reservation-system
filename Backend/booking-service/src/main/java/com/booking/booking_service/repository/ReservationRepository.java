package com.booking.booking_service.repository;

import com.booking.booking_service.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.booking.booking_service.enums.ReservationStatus;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, Date createdBefore);
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(
            value = "SELECT COUNT(rsi.stall_id) " +
                    "FROM reservation_stall_ids rsi " +
                    "JOIN reservation r ON r.id = rsi.reservation_id " +
                    "WHERE r.user_id = :userId " +
                    "AND r.exhibition_id = :exhibitionId " +
                    "AND r.status = 'CONFIRMED'",
            nativeQuery = true
    )
    int countUserBookedStalls(
            @Param("userId") Long userId,
            @Param("exhibitionId") Long exhibitionId
    );




}
