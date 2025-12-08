package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

  Genre findByStallId(Long id);

  Genre findByExhibitionIdAndStallId(Long exhibitionId, Long stallId);
}
