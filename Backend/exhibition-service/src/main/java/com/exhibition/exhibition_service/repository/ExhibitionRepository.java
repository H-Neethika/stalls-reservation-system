package com.exhibition.exhibition_service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exhibition.exhibition_service.enums.ExhibitionState;
import com.exhibition.exhibition_service.model.Exhibition;

@Repository

public interface ExhibitionRepository extends JpaRepository<Exhibition,Long> {

    List<Exhibition> findByExhibitionState(ExhibitionState state);
    List<Exhibition> findByOrganizerId(Long organizerId);

    boolean existsByExhibitionStateAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            ExhibitionState state,
            LocalDateTime endDateTime,
            LocalDateTime startDateTime
    );

    boolean existsByExhibitionStateAndIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            ExhibitionState state,
            Long id,
            LocalDateTime endDateTime,
            LocalDateTime startDateTime
    );

}
