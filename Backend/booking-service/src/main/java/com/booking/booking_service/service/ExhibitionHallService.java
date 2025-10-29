package com.booking.booking_service.service;

import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.request.CreateExhibitionHallRequest;

import java.util.List;
import java.util.Optional;

public interface ExhibitionHallService {

    ExhibitionHall createExhibitionHall(CreateExhibitionHallRequest exhibitionHallReq);
    List<ExhibitionHall> getAllExhibitionHalls();
    Optional<ExhibitionHall> getExhibitionHallById(Long id);
    ExhibitionHall updateExhibitionHall(Long id, CreateExhibitionHallRequest updatedExhibitionHall);
    void deleteExhibitionHall(Long id);

}
