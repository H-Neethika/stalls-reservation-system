package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import com.exhibition.exhibition_service.domain.EXHIBITION_STATE;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository

public interface ExhibitionRepository extends JpaRepository<Exhibition,Long> {

    List<Exhibition> findByExhibitionState(EXHIBITION_STATE state);

    boolean existsByExhibitionStateAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            EXHIBITION_STATE state,
            LocalDateTime endDateTime,
            LocalDateTime startDateTime
    );

    boolean existsByExhibitionStateAndIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            EXHIBITION_STATE state,
            Long id,
            LocalDateTime endDateTime,
            LocalDateTime startDateTime
    );

}
