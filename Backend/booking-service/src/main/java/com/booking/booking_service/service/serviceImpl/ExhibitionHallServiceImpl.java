package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.ExhibitionHall;
import com.booking.booking_service.model.Hall;
import com.booking.booking_service.model.StallType;
import com.booking.booking_service.repository.ExhibitionHallRepository;
import com.booking.booking_service.repository.HallRepository;
import com.booking.booking_service.repository.StallTypeRepository;
import com.booking.booking_service.request.CreateExhibitionHallRequest;
import com.booking.booking_service.request.StallTypeRequest;
import com.booking.booking_service.service.ExhibitionHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        newExhibitionHall.setRows(exhibitionHallReq.getRows());
        newExhibitionHall.setColumns(exhibitionHallReq.getColumns());

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
    public List<ExhibitionHall> getAllExhibitionHalls() {
        return exhibitionHallRepository.findAll();
    }

    @Override
    public Optional<ExhibitionHall> getExhibitionHallById(Long id) {
        return exhibitionHallRepository.findById(id);
    }

    @Override
    public ExhibitionHall updateExhibitionHall(Long id, CreateExhibitionHallRequest updatedExhibitionHall) {
        return exhibitionHallRepository.findById(id).map(existing -> {
            existing.setHallId(hallRepository.findById(updatedExhibitionHall.getHallId()).get());
            existing.setExhibitionId(updatedExhibitionHall.getExhibitionId());
            existing.setRows(updatedExhibitionHall.getRows());
            existing.setColumns(updatedExhibitionHall.getColumns());
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
}
