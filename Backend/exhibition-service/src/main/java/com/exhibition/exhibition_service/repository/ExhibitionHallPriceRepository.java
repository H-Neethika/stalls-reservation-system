package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.ExhibitionHall;
import com.exhibition.exhibition_service.model.ExhibitionHallPrice;
import com.exhibition.exhibition_service.model.StallType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionHallPriceRepository extends JpaRepository<ExhibitionHallPrice, Long> {
    List<ExhibitionHallPrice> findByExhibitionHall(ExhibitionHall hall);
    Optional<ExhibitionHallPrice> findByExhibitionHallAndStallType(ExhibitionHall hall, StallType stallType);
    Optional<ExhibitionHallPrice> findFirstByExhibitionHall_Hall_IdAndStallType(Long hallId, StallType stallType);
}
