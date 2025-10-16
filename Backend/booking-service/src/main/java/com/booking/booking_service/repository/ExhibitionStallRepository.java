package com.booking.booking_service.repository;

import com.booking.booking_service.model.ExhibitionStall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionStallRepository extends JpaRepository<ExhibitionStall,Long> {
    boolean existsByExhibitionIdAndStallId(Long exhibitionId, Long id);
}
