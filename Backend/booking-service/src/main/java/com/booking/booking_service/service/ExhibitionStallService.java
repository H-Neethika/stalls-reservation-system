package com.booking.booking_service.service;

import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Genre;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExhibitionStallService {

    public Page<ExhibitionStall> getExhibitionStall (Long hallId, String bookingStatus, String stallType, String genre, Long exhibitionId, Pageable page);

    public List<Genre> getExhibitionStallGenres(Long hallId, Long stallId);

    public List<ExhibitionStall> createExhibitionStall(CreateExhibitionStallRequest exhibitionStallReq);

    public ExhibitionStall updateExhibitionStall(Long stallId,Long exhibitionId, ExhibitionStall exhibitionStall);

    public  void deleteExhibitionStall(Long stallId,Long exhibitionId);

}
