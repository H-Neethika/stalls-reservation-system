package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.BookingStatus;
import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.StallType;
import com.booking.booking_service.repository.BookingStatusRepository;
import com.booking.booking_service.repository.ExhibitionHallRepository;
import com.booking.booking_service.repository.ExhibitionStallRepository;
import com.booking.booking_service.repository.StallTypeRepository;
import com.booking.booking_service.request.BulkCreateExhibitionStallsRequest;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import com.booking.booking_service.service.ExhibitionStallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExhibitionStallServiceImpl implements ExhibitionStallService {
    @Autowired
    private ExhibitionStallRepository exhibitionStallRepository;


    @Autowired
    private BookingStatusRepository bookingStatusRepository;

    @Autowired
    private ExhibitionHallRepository exhibitionHallRepository;

    @Autowired
    private StallTypeRepository stallTypeRepository;

    @Override
    public ExhibitionStall createExhibitionStall(CreateExhibitionStallRequest request) {

        ExhibitionHall hall = exhibitionHallRepository.findById(request.getExhibitionHallId())
                .orElseThrow(() -> new RuntimeException("ExhibitionHall not found with id " + request.getExhibitionHallId()));

        StallType stallType = stallTypeRepository.findById(request.getStallTypeId())
                .orElseThrow(() -> new RuntimeException("StallType not found with id " + request.getStallTypeId()));

        BookingStatus availableStatus = bookingStatusRepository.findByStatus("AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Default booking status 'AVAILABLE' not found"));

        // ✅ Create and save ExhibitionStall
        ExhibitionStall stall = new ExhibitionStall();
        stall.setExhibitionHallId(hall);
        stall.setStallName(request.getStallName());
        stall.setPrice(request.getPrice());
        stall.setRowPosition(request.getRowPosition());
        stall.setColumnPosition(request.getColumnPosition());
        stall.setBookingStatus(availableStatus);
        stall.setStallType(stallType); // link the selected StallType

        return exhibitionStallRepository.save(stall);
    }

    @Override
    public List<ExhibitionStall> createMultipleExhibitionStalls(BulkCreateExhibitionStallsRequest request) {

        ExhibitionHall hall = exhibitionHallRepository.findById(request.getExhibitionHallId())
                .orElseThrow(() -> new RuntimeException("ExhibitionHall not found with id " + request.getExhibitionHallId()));

        BookingStatus availableStatus = bookingStatusRepository.findByStatus("AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Default booking status 'AVAILABLE' not found"));

        List<ExhibitionStall> createdStalls = new ArrayList<>();

        for (CreateExhibitionStallRequest stallReq : request.getStalls()) {

            StallType stallType = stallTypeRepository.findById(stallReq.getStallTypeId())
                    .orElseThrow(() -> new RuntimeException("StallType not found with id " + stallReq.getStallTypeId()));

            ExhibitionStall stall = new ExhibitionStall();
            stall.setExhibitionHallId(hall);
            stall.setStallName(stallReq.getStallName());
            stall.setPrice(stallReq.getPrice());
            stall.setRowPosition(stallReq.getRowPosition());
            stall.setColumnPosition(stallReq.getColumnPosition());
            stall.setBookingStatus(availableStatus);
            stall.setStallType(stallType);

            createdStalls.add(exhibitionStallRepository.save(stall));
        }

        return createdStalls;
    }

    @Override
    public List<ExhibitionStall> getAllExhibitionStalls() {
        return exhibitionStallRepository.findAll();
    }

    @Override
    public Optional<ExhibitionStall> getExhibitionStallById(Long id) {
        return exhibitionStallRepository.findById(id);
    }

    @Override
    public ExhibitionStall updateExhibitionStall(Long id, CreateExhibitionStallRequest request) {
        return exhibitionStallRepository.findById(id).map(existing -> {

            StallType stallType = stallTypeRepository.findById(request.getStallTypeId())
                    .orElseThrow(() -> new RuntimeException("StallType not found with id " + request.getStallTypeId()));

            existing.setStallName(request.getStallName());
            existing.setPrice(request.getPrice());
            existing.setRowPosition(request.getRowPosition());
            existing.setColumnPosition(request.getColumnPosition());
            existing.setStallType(stallType);

            return exhibitionStallRepository.save(existing);

        }).orElseThrow(() -> new RuntimeException("ExhibitionStall not found with id " + id));
    }

    @Override
    public void deleteExhibitionStall(Long id) {
        if (!exhibitionStallRepository.existsById(id)) {
            throw new RuntimeException("ExhibitionStall not found with id " + id);
        }
        exhibitionStallRepository.deleteById(id);
    }
}
