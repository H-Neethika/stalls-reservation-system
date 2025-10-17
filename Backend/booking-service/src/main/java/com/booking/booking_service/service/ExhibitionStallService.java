package com.booking.booking_service.service;

import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Genre;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExhibitionStallService {

    public List<ExhibitionStall> createExhibitionStall(CreateExhibitionStallRequest exhibitionStallReq);

    public ExhibitionStall updateExhibitionStall(Long stallId, ExhibitionStall exhibitionStall);

    public  void deleteExhibitionStall(Long stallId);

    public Page<ExhibitionStall> getExhibitionStall (Long hallId,String bookingStatus, String stallType, String genre, Long exhibitionId);

    public Genre getGenre(String query);
}
