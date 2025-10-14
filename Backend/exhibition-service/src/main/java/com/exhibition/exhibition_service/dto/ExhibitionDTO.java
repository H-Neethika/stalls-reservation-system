package com.exhibition.exhibition_service.dto;

import com.exhibition.exhibition_service.domain.EXHIBITION_STATE;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExhibitionDTO {

    private Long id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime bookingOpenDateTime;
    private LocalDateTime bookingCloseDateTime;
    private int stallsPerPerson;
    private EXHIBITION_STATE exhibitionState;
}
