package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.Exhibition;
import com.exhibition.exhibition_service.model.ExhibitionStall;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionStallRepository extends JpaRepository<ExhibitionStall, Long> {
    List<ExhibitionStall> findByExhibition(Exhibition exhibition);
    List<ExhibitionStall> findByIdInAndExhibition(List<Long> ids, Exhibition exhibition);
    List<ExhibitionStall> findByStallId_Id(Long stallId);
    List<ExhibitionStall> findByExhibitionAndStall_IdIn(
        Exhibition exhibition,
        List<Long> stallIds
    );

    List<ExhibitionStall> findByExhibitionAndStall_Id(Exhibition exhibition, Long stallId);
}
