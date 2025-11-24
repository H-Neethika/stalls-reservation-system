package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class HallRef {
    private Long id;
    private String hallName;
    private List<ExhibitionHallPriceResponse> prices;
}
