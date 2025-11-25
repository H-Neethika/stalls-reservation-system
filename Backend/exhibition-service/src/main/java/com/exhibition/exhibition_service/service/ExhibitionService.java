package com.exhibition.exhibition_service.service;

import java.time.LocalDateTime;
import java.util.List;

import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.enums.ExhibitionState;

public interface ExhibitionService {

    ExhibitionDTO createExhibition(ExhibitionDTO exhibition);
    ExhibitionDTO updateExhibition(Long id,ExhibitionDTO exhibition, Long requesterUserId);

    void deleteExhibition(Long id);
    ExhibitionDTO getExhibitionById(Long id);
    List<ExhibitionDTO> getAllExhibitions();
    List<ExhibitionDTO> getExhibitionsByState(ExhibitionState state);
    List<com.exhibition.exhibition_service.dto.ExhibitionWithHallsResponse> getExhibitionsByOrganizer(Long organizerId);
    List<com.exhibition.exhibition_service.dto.ExhibitionWithHallsResponse> getExhibitionsByStateWithHalls(ExhibitionState state);
    List<com.exhibition.exhibition_service.dto.ExhibitionBriefResponse> getExhibitionsByDateRange(LocalDateTime start, LocalDateTime end);

}
