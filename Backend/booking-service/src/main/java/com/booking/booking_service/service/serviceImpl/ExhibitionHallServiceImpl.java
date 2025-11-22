package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.model.Hall;
import com.booking.booking_service.model.StallType;
import com.booking.booking_service.repository.ExhibitionHallRepository;
import com.booking.booking_service.repository.HallRepository;
import com.booking.booking_service.repository.StallTypeRepository;
import com.booking.booking_service.request.CreateExhibitionHallRequest;
import com.booking.booking_service.request.StallTypeRequest;
import com.booking.booking_service.response.ExhibitionHallResponse;
import com.booking.booking_service.response.StallTypeResponse;
import com.booking.booking_service.service.ExhibitionHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExhibitionHallServiceImpl implements ExhibitionHallService {
    @Autowired
    private ExhibitionHallRepository exhibitionHallRepository;


    @Autowired
    private StallTypeRepository stallTypeRepository;

    @Autowired
    private HallRepository hallRepository;

    @Override
    public ExhibitionHall createExhibitionHall(CreateExhibitionHallRequest exhibitionHallReq) {

        // 1️⃣ Find the Hall (parent)
        Hall hall = hallRepository.findById(exhibitionHallReq.getHallId())
                .orElseThrow(() -> new RuntimeException("Hall not found with id " + exhibitionHallReq.getHallId()));

        // 2️⃣ Create the ExhibitionHall
        ExhibitionHall newExhibitionHall = new ExhibitionHall();
        newExhibitionHall.setHallId(hall);
        newExhibitionHall.setExhibitionId(exhibitionHallReq.getExhibitionId());
//        newExhibitionHall.setRows(exhibitionHallReq.getRows());
//        newExhibitionHall.setColumns(exhibitionHallReq.getColumns());

        ExhibitionHall savedExhibitionHall = exhibitionHallRepository.save(newExhibitionHall);

        // 3️⃣ Create and save StallTypes (if provided)
        if (exhibitionHallReq.getStallTypes() != null && !exhibitionHallReq.getStallTypes().isEmpty()) {
            List<StallType> createdTypes = new ArrayList<>();

            for (StallTypeRequest stallTypeReq : exhibitionHallReq.getStallTypes()) {
                StallType stallType = new StallType();
                stallType.setType(stallTypeReq.getType());
                stallType.setPrice(stallTypeReq.getPrice());
                stallType.setExhibitionHallId(savedExhibitionHall); // link to this hall
                createdTypes.add(stallTypeRepository.save(stallType));
            }
        }

        return savedExhibitionHall;
    }
    @Override
    public List<ExhibitionHallResponse> getAllExhibitionHalls() {
        return exhibitionHallRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ExhibitionHallResponse> getExhibitionHallById(Long id) {
        return exhibitionHallRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Override
    public ExhibitionHall updateExhibitionHall(Long id, CreateExhibitionHallRequest updatedExhibitionHall) {
        return exhibitionHallRepository.findById(id).map(existing -> {
            existing.setHallId(hallRepository.findById(updatedExhibitionHall.getHallId()).get());
            existing.setExhibitionId(updatedExhibitionHall.getExhibitionId());
//            existing.setRows(updatedExhibitionHall.getRows());
//            existing.setColumns(updatedExhibitionHall.getColumns());
            return exhibitionHallRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("ExhibitionHall not found with id " + id));
    }

    @Override
    public void deleteExhibitionHall(Long id) {
        if (!exhibitionHallRepository.existsById(id)) {
            throw new RuntimeException("ExhibitionHall not found with id " + id);
        }
        exhibitionHallRepository.deleteById(id);
    }


    private ExhibitionHallResponse mapToResponse(ExhibitionHall hall) {

        Hall baseHall = hallRepository.findById(hall.getId()).get();
        ExhibitionHallResponse dto = new ExhibitionHallResponse();
        dto.setId(hall.getId());
        dto.setExhibitionId(hall.getExhibitionId());
        dto.setHallId(hall.getHallId().getId());
        dto.setHallName(hall.getHallId().getHallName());
        dto.setRows(baseHall.getRows());
        dto.setColumns(baseHall.getColumns());

        // ✅ Fetch all stall types for this hall
        List<StallTypeResponse> stallTypeResponses = stallTypeRepository
                .findByExhibitionHallId(hall) // assumes repository method exists
                .stream()
                .map(st -> {
                    StallTypeResponse stDto = new StallTypeResponse();
                    stDto.setId(st.getId());
                    stDto.setType(st.getType());
                    stDto.setPrice(st.getPrice());
                    return stDto;
                })
                .collect(Collectors.toList());

        dto.setStallTypes(stallTypeResponses);
        return dto;
    }
}
