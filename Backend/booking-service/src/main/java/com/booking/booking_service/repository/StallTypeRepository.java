package com.booking.booking_service.repository;

import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.model.StallType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StallTypeRepository extends JpaRepository <StallType,Long> {
    List<StallType> findByExhibitionHallId(ExhibitionHall exhibitionHall);

}
