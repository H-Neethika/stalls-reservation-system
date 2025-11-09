package com.booking.booking_service.service;

import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.request.BulkCreateExhibitionStallsRequest;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import com.booking.booking_service.response.ExhibitionStallResponse;

import java.util.List;
import java.util.Optional;

public interface ExhibitionStallService {
    ExhibitionStall createExhibitionStall(CreateExhibitionStallRequest request);


    List<ExhibitionStall> createMultipleExhibitionStalls(BulkCreateExhibitionStallsRequest request);

    List<ExhibitionStallResponse> getAllExhibitionStalls();
    Optional<ExhibitionStallResponse> getExhibitionStallById(Long id);

    ExhibitionStall updateExhibitionStall(Long id, CreateExhibitionStallRequest request);

    void deleteExhibitionStall(Long id);

}
