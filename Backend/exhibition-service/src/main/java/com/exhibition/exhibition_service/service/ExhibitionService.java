package com.exhibition.exhibition_service.service;

import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.model.Exhibition;
import com.exhibition.exhibition_service.enums.ExhibitionState;

import java.util.List;

public interface ExhibitionService {

    ExhibitionDTO createExhibition(ExhibitionDTO exhibition);
    ExhibitionDTO updateExhibition(Long id,ExhibitionDTO exhibition, Long requesterUserId);

    void deleteExhibition(Long id);
    ExhibitionDTO getExhibitionById(Long id);
    List<ExhibitionDTO> getAllExhibitions();
    List<ExhibitionDTO> getExhibitionsByState(ExhibitionState state);
    List<com.exhibition.exhibition_service.dto.ExhibitionWithHallsResponse> getExhibitionsByOrganizer(Long organizerId);

}
