package com.exhibition.exhibition_service.dto;

import com.exhibition.exhibition_service.enums.ExhibitionState;
import java.util.List;
import lombok.Data;

@Data
public class ExhibitionLayoutResponse {
    private Long id;
    private String exhibitionName;
    private ExhibitionState exhibitionState;
    private List<HallLayoutResponse> halls;
}
