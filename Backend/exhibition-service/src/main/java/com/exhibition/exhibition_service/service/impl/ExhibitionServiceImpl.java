package com.exhibition.exhibition_service.service.impl;


import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.exception.InvalidExhibitionDateException;
import com.exhibition.exhibition_service.exception.ExhibitionConflictException;
import com.exhibition.exhibition_service.mapper.ExhibitionMapper;
import com.exhibition.exhibition_service.model.Exhibition;
import com.exhibition.exhibition_service.repository.ExhibitionRepository;
import com.exhibition.exhibition_service.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import static com.exhibition.exhibition_service.domain.EXHIBITION_STATE.PUBLISHED;

@Service
@RequiredArgsConstructor

public class ExhibitionServiceImpl implements ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionMapper exhibitionMapper;



    private void validateExhibitionDates(Exhibition exhibition) {

        LocalDateTime now = LocalDateTime.now();


        if (exhibition.getStartDateTime().isBefore(now)) {
            throw new InvalidExhibitionDateException("Start date/time cannot be in the past");
        }
        if (exhibition.getEndDateTime().isBefore(now)) {
            throw new InvalidExhibitionDateException("End date/time cannot be in the past");
        }
        if (exhibition.getBookingOpenDateTime().isBefore(now)) {
            throw new InvalidExhibitionDateException("Booking open date/time cannot be in the past");
        }
        if (exhibition.getBookingCloseDateTime().isBefore(now)) {
            throw new InvalidExhibitionDateException("Booking close date/time cannot be in the past");
        }

        // (1) endDateTime > startDateTime
        if (exhibition.getEndDateTime().isBefore(exhibition.getStartDateTime()) ||
                exhibition.getEndDateTime().isEqual(exhibition.getStartDateTime())) {
            throw new InvalidExhibitionDateException(" End date/time must be after Start date/time");
        }

        // (2) bookingCloseDateTime > bookingOpenDateTime
        if (exhibition.getBookingCloseDateTime().isBefore(exhibition.getBookingOpenDateTime()) ||
                exhibition.getBookingCloseDateTime().isEqual(exhibition.getBookingOpenDateTime())) {
            throw new InvalidExhibitionDateException(" Booking close date/time must be after Booking open date/time");
        }

        // (3) bookingCloseDateTime >= startDateTime + 24 hours
        Duration gapBetweenBookingCloseAndStart = Duration.between(
                exhibition.getBookingCloseDateTime(), exhibition.getStartDateTime());

        if (gapBetweenBookingCloseAndStart.toHours() < 24) {
            throw new InvalidExhibitionDateException(
                    " There must be at least 24 hours gap between Booking Close and Exhibition Start time");
        }

        // (4) bookingCloseDateTime < endDateTime
        if (exhibition.getBookingCloseDateTime().isAfter(exhibition.getEndDateTime()) ||
                exhibition.getBookingCloseDateTime().isEqual(exhibition.getEndDateTime())) {
            throw new InvalidExhibitionDateException(
                    " Booking Close time must be before Exhibition End time");
        }
    }

    private void validateNoOverlapWithPublished(Exhibition exhibition, Long excludeIdIfAny) {
        if (exhibition.getExhibitionState() != PUBLISHED) {
            return; // Only enforce overlap when target is PUBLISHED
        }

        boolean exists;
        if (excludeIdIfAny == null) {
            exists = exhibitionRepository
                    .existsByExhibitionStateAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                            PUBLISHED,
                            exhibition.getEndDateTime(),
                            exhibition.getStartDateTime());
        } else {
            exists = exhibitionRepository
                    .existsByExhibitionStateAndIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                            PUBLISHED,
                            excludeIdIfAny,
                            exhibition.getEndDateTime(),
                            exhibition.getStartDateTime());
        }

        if (exists) {
            throw new ExhibitionConflictException("Another exhibition is already published during these dates.");
        }
    }


    @Override
    public ExhibitionDTO createExhibition(ExhibitionDTO exhibition) {
        Exhibition entity = exhibitionMapper.toEntity(exhibition);
        validateExhibitionDates(entity);
        validateNoOverlapWithPublished(entity, null);
        Exhibition saved = exhibitionRepository.save(entity);
        return exhibitionMapper.toDto(saved);
    }

    @Override
    public ExhibitionDTO updateExhibition(Long id, ExhibitionDTO exhibition) {
        return exhibitionRepository.findById(id).map(existing ->{

            if (exhibition.getExhibitionName() != null && !exhibition.getExhibitionName().isBlank())
                existing.setExhibitionName(exhibition.getExhibitionName());

            if (exhibition.getStartDateTime() != null)
                existing.setStartDateTime(exhibition.getStartDateTime());

            if (exhibition.getEndDateTime() != null)
                existing.setEndDateTime(exhibition.getEndDateTime());

            if (exhibition.getBookingOpenDateTime() != null)
                existing.setBookingOpenDateTime(exhibition.getBookingOpenDateTime());

            if (exhibition.getBookingCloseDateTime() != null)
                existing.setBookingCloseDateTime(exhibition.getBookingCloseDateTime());

            if (exhibition.getStallsPerPerson() != 0)
                existing.setStallsPerPerson(exhibition.getStallsPerPerson());

            if (exhibition.getExhibitionState() != null)
                existing.setExhibitionState(exhibition.getExhibitionState());


            validateExhibitionDates(existing);
            validateNoOverlapWithPublished(existing, existing.getId());
            Exhibition updated = exhibitionRepository.save(existing);
            return exhibitionMapper.toDto(updated);
        }).orElseThrow(()->new RuntimeException("Exhibition not found with id "+id));
    }

    @Override
    public void deleteExhibition(Long id) {
        if(!exhibitionRepository.existsById(id)){

            throw new RuntimeException("Exhibition not found with id "+id);
        }

        exhibitionRepository.deleteById(id);

    }

    @Override
    public ExhibitionDTO getExhibitionById(Long id) {

        return exhibitionRepository.findById(id)
                .map(exhibitionMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Exhibition not found with id " + id));

    }

    @Override
    public List<ExhibitionDTO> getAllExhibitions() {
        return exhibitionRepository.findAll().stream().map(exhibitionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ExhibitionDTO> getExhibitionsByState(com.exhibition.exhibition_service.domain.EXHIBITION_STATE state) {
        return exhibitionRepository.findByExhibitionState(state)
                .stream()
                .map(exhibitionMapper::toDto)
                .collect(Collectors.toList());
    }
}
