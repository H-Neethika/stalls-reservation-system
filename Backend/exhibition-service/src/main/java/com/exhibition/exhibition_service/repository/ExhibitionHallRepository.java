package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.ExhibitionHall;
import com.exhibition.exhibition_service.model.Exhibition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionHallRepository extends JpaRepository<ExhibitionHall, Long> {
    List<ExhibitionHall> findByExhibition(Exhibition exhibition);
}
