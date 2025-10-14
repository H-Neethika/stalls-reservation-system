package com.exhibition.exhibition_service.service;

import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.model.Exhibition;

import java.util.List;

public interface ExhibitionService {

    ExhibitionDTO createExhibition(ExhibitionDTO exhibition);
    ExhibitionDTO updateExhibition(Long id,ExhibitionDTO exhibition);

    void deleteExhibition(Long id);
    ExhibitionDTO getExhibitionById(Long id);
    List<ExhibitionDTO> getAllExhibitions();

}
