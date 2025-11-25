package com.exhibition.exhibition_service.dto;

import com.exhibition.exhibition_service.enums.ExhibitionState;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ExhibitionWithHallsResponse {
    private Long id;
    private Long organizerId;
    private String exhibitionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime bookingOpenDateTime;
    private LocalDateTime bookingCloseDateTime;
    private int stallsPerPerson;
    private ExhibitionState exhibitionState;
    private List<HallRef> halls;
}
