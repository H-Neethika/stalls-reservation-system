package com.booking.booking_service.response;

import lombok.Data;

@Data
public class ExhibitionStallResponse {
    private Long id;
    private String stallName;
    private Long price;
    private Long rowPosition;
    private Long columnPosition;

    private Long exhibitionHallId;
    private String hallName;

    private Long stallTypeId;
    private String stallTypeName;

    private String bookingStatus;
    private String bookingColor;
}
