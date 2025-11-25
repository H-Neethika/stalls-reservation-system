package com.exhibition.exhibition_service.mapper;


import com.exhibition.exhibition_service.enums.ExhibitionState;
import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.model.Exhibition;
import org.springframework.stereotype.Component;

@Component

public class ExhibitionMapper {

    public ExhibitionDTO toDto(Exhibition entity) {
        if (entity == null) return null;
        return ExhibitionDTO.builder()
                .id(entity.getId())
                .organizerId(entity.getOrganizerId())
                .exhibitionName(entity.getExhibitionName())
                .startDateTime(entity.getStartDateTime())
                .endDateTime(entity.getEndDateTime())
                .bookingOpenDateTime(entity.getBookingOpenDateTime())
                .bookingCloseDateTime(entity.getBookingCloseDateTime())
                .stallsPerPerson(entity.getStallsPerPerson())
                .exhibitionState(entity.getExhibitionState())
                .build();
    }

    public Exhibition toEntity(ExhibitionDTO dto) {
        if (dto == null) return null;
        return Exhibition.builder()
                .id(dto.getId())
                .organizerId(dto.getOrganizerId())
                .exhibitionName(dto.getExhibitionName())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .bookingOpenDateTime(dto.getBookingOpenDateTime())
                .bookingCloseDateTime(dto.getBookingCloseDateTime())
                .stallsPerPerson(dto.getStallsPerPerson())
                .exhibitionState(dto.getExhibitionState() != null
                        ? dto.getExhibitionState()
                        : ExhibitionState.DRAFT)
                .build();
    }
}
