package com.exhibition.exhibition_service.dto;

import com.exhibition.exhibition_service.domain.EXHIBITION_STATE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExhibitionDTO {

    private Long id;
    private Long organizerId;
    private String exhibitionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime bookingOpenDateTime;
    private LocalDateTime bookingCloseDateTime;
    private int stallsPerPerson;
    private EXHIBITION_STATE exhibitionState;
    private List<Long> hallIds;
}
