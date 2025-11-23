package com.exhibition.exhibition_service.dto;

import com.exhibition.exhibition_service.enums.EXHIBITION_STATE;
import java.util.List;
import lombok.Data;

@Data
public class ExhibitionLayoutResponse {
    private Long id;
    private String exhibitionName;
    private EXHIBITION_STATE exhibitionState;
    private List<HallLayoutResponse> halls;
}
