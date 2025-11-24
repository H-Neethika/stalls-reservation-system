package com.booking.booking_service.dto.response;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long exhibitionId;
    private Long totalAmount;
    private Date createdAt;
    private String status;
    private List<ReservedStallResponse> stalls;
}
