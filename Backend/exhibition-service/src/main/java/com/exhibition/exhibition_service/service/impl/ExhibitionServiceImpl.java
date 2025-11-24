package com.exhibition.exhibition_service.service.impl;

import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.dto.ExhibitionWithHallsResponse;
import com.exhibition.exhibition_service.dto.ExhibitionHallPriceResponse;
import com.exhibition.exhibition_service.dto.ExhibitionBriefResponse;
import com.exhibition.exhibition_service.dto.HallPriceDTO;
import com.exhibition.exhibition_service.dto.HallRef;
import com.exhibition.exhibition_service.exception.ExhibitionForbiddenException;
import com.exhibition.exhibition_service.exception.InvalidExhibitionDateException;
import com.exhibition.exhibition_service.exception.ExhibitionConflictException;
import com.exhibition.exhibition_service.mapper.ExhibitionMapper;
import com.exhibition.exhibition_service.model.Exhibition;
import com.exhibition.exhibition_service.model.ExhibitionHall;
import com.exhibition.exhibition_service.model.ExhibitionHallPrice;
import com.exhibition.exhibition_service.repository.ExhibitionRepository;
import com.exhibition.exhibition_service.repository.ExhibitionHallRepository;
import com.exhibition.exhibition_service.repository.ExhibitionHallPriceRepository;
import com.exhibition.exhibition_service.service.ExhibitionService;
import com.exhibition.exhibition_service.service.LayoutService;
import com.exhibition.exhibition_service.enums.ExhibitionState;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import static com.exhibition.exhibition_service.enums.ExhibitionState.PUBLISHED;

@Service
@RequiredArgsConstructor

public class ExhibitionServiceImpl implements ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionHallRepository exhibitionHallRepository;
    private final ExhibitionHallPriceRepository exhibitionHallPriceRepository;
    private final ExhibitionMapper exhibitionMapper;
    private final LayoutService layoutService;



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

        validateExhibitionOrdering(exhibition);
    }

    private void validateExhibitionOrdering(Exhibition exhibition) {
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
    public ExhibitionDTO createExhibition(ExhibitionDTO exhibitionDTO) {
        Exhibition entity = exhibitionMapper.toEntity(exhibitionDTO);
        validateExhibitionDates(entity);
        validateNoOverlapWithPublished(entity, null);
        Exhibition exhibition = exhibitionRepository.save(entity);
        if (exhibitionDTO.getHallIds() != null && !exhibitionDTO.getHallIds().isEmpty()) {
            layoutService.attachHallsWithStalls(exhibition, exhibitionDTO.getHallIds());
        }
        List<HallPriceDTO> hallPrices = exhibitionDTO.getHallPrices();
        if (hallPrices != null && !hallPrices.isEmpty()) {
            layoutService.upsertHallPrices(exhibition, hallPrices);
        }
        return exhibitionMapper.toDto(exhibition);
    }

    /**
     * Promote draft exhibitions to PUBLISHED when booking window has opened.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedDelayString = "600000")
    @Transactional
    public void autoPublishOpenedExhibitions() {
        LocalDateTime now = LocalDateTime.now();
        exhibitionRepository.findByExhibitionState(ExhibitionState.DRAFT).stream()
                .filter(e -> e.getBookingOpenDateTime() != null && !e.getBookingOpenDateTime().isAfter(now))
                .forEach(e -> {
                    e.setExhibitionState(ExhibitionState.PUBLISHED);
                    validateExhibitionOrdering(e);
                    validateNoOverlapWithPublished(e, e.getId());
                    exhibitionRepository.save(e);
                });

        exhibitionRepository.findByExhibitionState(ExhibitionState.PUBLISHED).stream()
                .filter(e -> e.getBookingCloseDateTime() != null && !e.getBookingCloseDateTime().isAfter(now))
                .forEach(e -> {
                    e.setExhibitionState(ExhibitionState.CLOSED);
                    exhibitionRepository.save(e);
                });
    }

    @Override
    public ExhibitionDTO updateExhibition(Long id, ExhibitionDTO exhibition, Long requesterUserId) {
        return exhibitionRepository.findById(id).map(existing ->{

            if (existing.getOrganizerId() == null || requesterUserId == null || !existing.getOrganizerId().equals(requesterUserId)) {
                throw new com.exhibition.exhibition_service.exception.ExhibitionForbiddenException("Only the created  organizer can update this exhibition");
            }

            if (exhibition.getOrganizerId() != null && !exhibition.getOrganizerId().equals(existing.getOrganizerId())) {
                throw new ExhibitionForbiddenException("Organizer cannot be changed once created");
            }

            boolean isDraft = existing.getExhibitionState() == ExhibitionState.DRAFT;
            boolean hasScheduleChanges = exhibition.getStartDateTime() != null
                    || exhibition.getEndDateTime() != null
                    || exhibition.getBookingOpenDateTime() != null
                    || exhibition.getBookingCloseDateTime() != null
                    || exhibition.getStallsPerPerson() != 0;
            boolean hasPriceChanges = exhibition.getHallPrices() != null && !exhibition.getHallPrices().isEmpty();

            if (!isDraft && (hasScheduleChanges || hasPriceChanges)) {
                throw new ExhibitionForbiddenException("Schedule, capacity, and pricing can only be updated while exhibition is in DRAFT state");
            }

            if (exhibition.getExhibitionName() != null && !exhibition.getExhibitionName().isBlank())
                existing.setExhibitionName(exhibition.getExhibitionName());

            if (isDraft && exhibition.getStartDateTime() != null)
                existing.setStartDateTime(exhibition.getStartDateTime());

            if (isDraft && exhibition.getEndDateTime() != null)
                existing.setEndDateTime(exhibition.getEndDateTime());

            if (isDraft && exhibition.getBookingOpenDateTime() != null)
                existing.setBookingOpenDateTime(exhibition.getBookingOpenDateTime());

            if (isDraft && exhibition.getBookingCloseDateTime() != null)
                existing.setBookingCloseDateTime(exhibition.getBookingCloseDateTime());

            if (isDraft && exhibition.getStallsPerPerson() != 0)
                existing.setStallsPerPerson(exhibition.getStallsPerPerson());

            if (exhibition.getExhibitionState() != null)
                existing.setExhibitionState(exhibition.getExhibitionState());


            if (isDraft && (hasScheduleChanges || exhibition.getExhibitionState() != null)) {
                validateExhibitionDates(existing);
                validateNoOverlapWithPublished(existing, existing.getId());
            }
            Exhibition updated = exhibitionRepository.save(existing);

            // Upsert hall prices if provided
            List<HallPriceDTO> hallPrices = exhibition.getHallPrices();
            if (isDraft && hallPrices != null && !hallPrices.isEmpty()) {
                layoutService.upsertHallPrices(updated, hallPrices);
            }

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
    public List<ExhibitionDTO> getExhibitionsByState(ExhibitionState state) {
        return exhibitionRepository.findByExhibitionState(state)
                .stream()
                .map(exhibitionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExhibitionWithHallsResponse> getExhibitionsByStateWithHalls(ExhibitionState state) {
        return exhibitionRepository.findByExhibitionState(state)
                .stream()
                .map(this::toWithHalls)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExhibitionBriefResponse> getExhibitionsByDateRange(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        return exhibitionRepository.findAll().stream()
                .filter(e -> start == null || !e.getStartDateTime().isBefore(start))
                .filter(e -> end == null || !e.getEndDateTime().isAfter(end))
                .filter(e -> e.getExhibitionState() == ExhibitionState.DRAFT
                        || e.getExhibitionState() == ExhibitionState.PUBLISHED
                        || (e.getEndDateTime() != null && e.getEndDateTime().isAfter(now)))
                .map(this::toBrief)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExhibitionWithHallsResponse> getExhibitionsByOrganizer(Long organizerId) {
        if (organizerId == null) {
            throw new IllegalArgumentException("organizerId is required");
        }
        return exhibitionRepository.findByOrganizerId(organizerId)
                .stream()
                .map(this::toWithHalls)
                .collect(Collectors.toList());
    }

    private ExhibitionWithHallsResponse toWithHalls(Exhibition exhibition) {
        ExhibitionWithHallsResponse dto = new ExhibitionWithHallsResponse();
        dto.setId(exhibition.getId());
        dto.setOrganizerId(exhibition.getOrganizerId());
        dto.setExhibitionName(exhibition.getExhibitionName());
        dto.setStartDateTime(exhibition.getStartDateTime());
        dto.setEndDateTime(exhibition.getEndDateTime());
        dto.setBookingOpenDateTime(exhibition.getBookingOpenDateTime());
        dto.setBookingCloseDateTime(exhibition.getBookingCloseDateTime());
        dto.setStallsPerPerson(exhibition.getStallsPerPerson());
        dto.setExhibitionState(exhibition.getExhibitionState());
        List<ExhibitionHall> halls = exhibitionHallRepository.findByExhibition(exhibition);
        List<HallRef> hallRefs = halls.stream().map(h -> {
            HallRef ref = new HallRef();
            ref.setId(h.getHall().getId());
            ref.setHallName(h.getHall().getHallName());
            List<ExhibitionHallPriceResponse> prices = exhibitionHallPriceRepository.findByExhibitionHall(h).stream()
                    .map(this::toPriceResponse)
                    .collect(Collectors.toList());
            ref.setPrices(prices);
            return ref;
        }).collect(Collectors.toList());
        dto.setHalls(hallRefs);
        return dto;
    }

    private ExhibitionBriefResponse toBrief(Exhibition exhibition) {
        ExhibitionBriefResponse dto = new ExhibitionBriefResponse();
        dto.setExhibitionId(exhibition.getId());
        dto.setExhibitionName(exhibition.getExhibitionName());
        dto.setStartDateTime(exhibition.getStartDateTime());
        dto.setEndDateTime(exhibition.getEndDateTime());
        return dto;
    }

    private ExhibitionHallPriceResponse toPriceResponse(ExhibitionHallPrice price) {
        ExhibitionHallPriceResponse dto = new ExhibitionHallPriceResponse();
        dto.setId(price.getId());
        dto.setExhibitionHallId(price.getExhibitionHall().getId());
        dto.setHallName(price.getExhibitionHall().getHall().getHallName());
        dto.setStallTypeId(price.getStallType().getId());
        dto.setStallType(price.getStallType().getType());
        dto.setPrice(price.getPrice());
        return dto;
    }
}
