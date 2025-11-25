package com.exhibition.exhibition_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class OrganizerLayoutResponse {
    private List<ExhibitionLayoutResponse> exhibitions;
}
