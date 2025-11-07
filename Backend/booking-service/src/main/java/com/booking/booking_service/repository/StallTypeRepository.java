package com.booking.booking_service.repository;

import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.model.StallType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StallTypeRepository extends JpaRepository <StallType,Long> {
    Optional<StallType> findByExhibitionHallId(ExhibitionHall exhibitionHall);

}
