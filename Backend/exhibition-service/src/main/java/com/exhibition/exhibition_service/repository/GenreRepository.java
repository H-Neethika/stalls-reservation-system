package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

  Genre findByStallIdAndReservationId(Long id, Long reservationId);

//  Genre findByExhibitionIdAndStallId(Long exhibitionId, Long stallId);
  List<Genre> findByExhibitionIdAndStallId(Long exhibitionId, Long stallId);

}
