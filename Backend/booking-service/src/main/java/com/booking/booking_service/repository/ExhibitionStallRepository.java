package com.booking.booking_service.repository;

import com.booking.booking_service.model.ExhibitionStall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExhibitionStallRepository extends JpaRepository<ExhibitionStall,Long> {
    boolean existsByExhibitionIdAndStallId(Long exhibitionId, Long id);
    Optional<ExhibitionStall> findByExhibitionIdAndStallId(Long exhibitionId, Long stallId);

    @Query("""
    SELECT e FROM ExhibitionStall e
    WHERE (:hallId IS NULL OR e.hallId = :hallId)
      AND (:exhibitionId IS NULL OR e.exhibitionId = :exhibitionId)
      AND (:bookingStatus IS NULL OR e.bookingStatus.status = :bookingStatus)
      AND (:stallType IS NULL OR e.stallType = :stallType)
      AND (:genre IS NULL OR EXISTS (
            SELECT g FROM e.genres g WHERE g.name = :genre
      ))
""")
    Page<ExhibitionStall> findAllByFilters(
            @Param("hallId") Long hallId,
            @Param("bookingStatus") String bookingStatus,
            @Param("stallType") String stallType,
            @Param("genre") String genre,
            @Param("exhibitionId") Long exhibitionId,
            Pageable pageable);

    List<ExhibitionStall> findByHallId(Long hallId);

    Optional<ExhibitionStall> findByStallId(Long stallId);
}
