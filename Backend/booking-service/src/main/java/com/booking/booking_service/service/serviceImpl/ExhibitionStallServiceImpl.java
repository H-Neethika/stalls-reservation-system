package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.dto.StallDto;
import com.booking.booking_service.model.BookingStatus;
import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.model.StallType;
import com.booking.booking_service.model.User;
import com.booking.booking_service.repository.BookingStatusRepository;
import com.booking.booking_service.repository.ExhibitionHallRepository;
import com.booking.booking_service.repository.ExhibitionStallRepository;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.repository.StallTypeRepository;
import com.booking.booking_service.request.BulkCreateExhibitionStallsRequest;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import com.booking.booking_service.request.ReservationRequest;
import com.booking.booking_service.response.ExhibitionStallResponse;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.response.PaymentSuccessResponse;
import com.booking.booking_service.response.UserResponse;
import com.booking.booking_service.service.ExhibitionStallService;
import com.booking.booking_service.service.UserService;
import jakarta.transaction.Transactional;
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

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserService userService;

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

    private ExhibitionStallResponse mapToResponse(ExhibitionStall stall) {
        ExhibitionStallResponse dto = new ExhibitionStallResponse();
        dto.setId(stall.getId());
        dto.setStallName(stall.getStallName());
        dto.setPrice(stall.getPrice());
        dto.setRowPosition(stall.getRowPosition());
        dto.setColumnPosition(stall.getColumnPosition());

        if (stall.getExhibitionHallId() != null) {
            dto.setExhibitionHallId(stall.getExhibitionHallId().getId());
            if (stall.getExhibitionHallId().getHallId() != null) {
                dto.setHallName(stall.getExhibitionHallId().getHallId().getHallName());
            }
        }

        if (stall.getStallType() != null) {
            dto.setStallTypeId(stall.getStallType().getId());
            dto.setStallTypeName(stall.getStallType().getType());
        }

        if (stall.getBookingStatus() != null) {
            dto.setBookingStatus(stall.getBookingStatus().getStatus());
            dto.setBookingColor(stall.getBookingStatus().getColor());
        }

        return dto;
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
    public List<ExhibitionStallResponse> getAllExhibitionStalls() {
        return exhibitionStallRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Optional<ExhibitionStallResponse> getExhibitionStallById(Long id) {
        return exhibitionStallRepository.findById(id)
                .map(this::mapToResponse);
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

    @Override
    @Transactional
    public PaymentSuccessResponse updateStallBookingStatus(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).get();
        List<ExhibitionStall> exhibitionStalls = reservation.getStall();
        for(ExhibitionStall stall: exhibitionStalls){
            stall.setBookingStatus(bookingStatusRepository.findById(3L).get());

        }
        reservation.setStall(exhibitionStalls);
        reservationRepository.save(reservation);

        UserResponse user = userService.getUserById(reservation.getUserId()).getBody();
        PaymentSuccessResponse response = new PaymentSuccessResponse();
        response.setReservationId(reservation.getId());
        response.setUserId(user.getId());
        response.setUsername(user.getName());
        response.setEmail(user.getEmail());
        response.setBookingDateTime(reservation.getCreatedAt());

        List<ExhibitionStall> reservationStalls  = reservation.getStall();
        List<StallDto> stallDetails = new ArrayList<>();
        for(ExhibitionStall stall: reservationStalls){
            StallDto stallDto = new StallDto();
            stallDto.setStallName(stall.getStallName());
            stallDto.setStallSize(stall.getStallType().getType());
            stallDetails.add(stallDto);
        }

        response.setStalls(stallDetails);

        return response;
    }
}
