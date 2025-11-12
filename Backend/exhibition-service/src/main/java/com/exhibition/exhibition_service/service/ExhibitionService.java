package com.exhibition.exhibition_service.service;

import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.model.Exhibition;
import com.exhibition.exhibition_service.domain.EXHIBITION_STATE;

import java.util.List;

public interface ExhibitionService {

    ExhibitionDTO createExhibition(ExhibitionDTO exhibition);
    ExhibitionDTO updateExhibition(Long id,ExhibitionDTO exhibition, Long requesterUserId);

    void deleteExhibition(Long id);
    ExhibitionDTO getExhibitionById(Long id);
    List<ExhibitionDTO> getAllExhibitions();
    List<ExhibitionDTO> getExhibitionsByState(EXHIBITION_STATE state);
    List<ExhibitionDTO> getExhibitionsByUserId(Long userId);

}
