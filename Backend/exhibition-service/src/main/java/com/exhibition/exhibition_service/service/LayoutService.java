package com.exhibition.exhibition_service.service;

import com.exhibition.exhibition_service.dto.*;
import com.exhibition.exhibition_service.enums.BookingStatus;
import com.exhibition.exhibition_service.model.Exhibition;
import com.exhibition.exhibition_service.model.ExhibitionHall;
import com.exhibition.exhibition_service.model.ExhibitionHallPrice;
import com.exhibition.exhibition_service.model.ExhibitionStall;
import com.exhibition.exhibition_service.model.Hall;
import com.exhibition.exhibition_service.model.Point;
import com.exhibition.exhibition_service.model.Stall;
import com.exhibition.exhibition_service.model.StallType;
import com.exhibition.exhibition_service.repository.ExhibitionHallRepository;
import com.exhibition.exhibition_service.repository.ExhibitionHallPriceRepository;
import com.exhibition.exhibition_service.repository.ExhibitionRepository;
import com.exhibition.exhibition_service.repository.ExhibitionStallRepository;
import com.exhibition.exhibition_service.repository.HallRepository;
import com.exhibition.exhibition_service.repository.StallRepository;
import com.exhibition.exhibition_service.repository.StallTypeRepository;
import com.exhibition.exhibition_service.messaging.event.StallStatusChangedEvent;
import com.exhibition.exhibition_service.messaging.producer.StallStatusEventProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LayoutService {

    private final HallRepository hallRepository;
    private final ExhibitionHallRepository exhibitionHallRepository;
    private final StallTypeRepository stallTypeRepository;
    private final StallRepository stallRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionStallRepository exhibitionStallRepository;
    private final ExhibitionHallPriceRepository exhibitionHallPriceRepository;
    private final StallStatusEventProducer stallStatusEventProducer;

    public List<HallSummaryResponse> getHallSummaries() {
        return hallRepository.findAll().stream()
                .map(this::toHallSummary)
                .collect(Collectors.toList());
    }

    public HallLayoutResponse getHallLayout(Long hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + hallId));
        HallLayoutResponse dto = new HallLayoutResponse();
        dto.setHallId(hall.getId());
        dto.setHallName(hall.getHallName());
        List<Stall> stalls = stallRepository.findByHall(hall);
        dto.setStalls(stalls.stream()
                .map(this::toStallLayout)
                .collect(Collectors.toList()));
        return dto;
    }

    public List<HallLayoutResponse> getAllHallLayouts() {
        return hallRepository.findAll().stream()
                .map(h -> {
                    HallLayoutResponse dto = new HallLayoutResponse();
                    dto.setHallId(h.getId());
                    dto.setHallName(h.getHallName());
                    List<Stall> stalls = stallRepository.findByHall(h);
                    dto.setStalls(stalls.stream()
                            .map(this::toStallLayout)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ExhibitionHallLayoutResponse> getExhibitionHallLayouts(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + exhibitionId));
        List<ExhibitionStall> exhibitionStalls = exhibitionStallRepository.findByExhibition(exhibition);
        Map<Long, List<ExhibitionStall>> stallsByHall = exhibitionStalls.stream()
                .filter(es -> es.getStall() != null && es.getStall().getHall() != null)
                .collect(Collectors.groupingBy(es -> es.getStall().getHall().getId()));

        List<ExhibitionHall> halls = exhibitionHallRepository.findByExhibition(exhibition);
        return halls.stream().map(h -> {
            ExhibitionHallLayoutResponse dto = new ExhibitionHallLayoutResponse();
            dto.setExhibitionHallId(h.getId());
            if (h.getHall() != null) {
                dto.setHallId(h.getHall().getId());
                dto.setHallName(h.getHall().getHallName());
            }
            List<ExhibitionStall> hallStalls = stallsByHall.getOrDefault(
                    h.getHall() != null ? h.getHall().getId() : null, List.of());
            dto.setStalls(hallStalls.stream()
                    .map(this::toStallSimpleLayout)
                    .collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    private StallSimpleLayoutResponse toStallSimpleLayout(ExhibitionStall es) {
        StallSimpleLayoutResponse dto = new StallSimpleLayoutResponse();
        dto.setExhibitionStallId(es.getId());
        dto.setStatus(es.getBookingStatus() != null ? es.getBookingStatus().name() : null);
        if (es.getStall() != null) {
            dto.setStallId(es.getStall().getId());
        }
        return dto;
    }

    @Transactional
    public void attachHallsWithStalls(Exhibition exhibition, List<Long> hallIds) {
        if (exhibition == null || exhibition.getId() == null) {
            throw new IllegalArgumentException("Exhibition must be persisted before attaching halls.");
        }
        BookingStatus available = BookingStatus.AVAILABLE;
        for (Long hallId : hallIds) {
            Hall hall = hallRepository.findById(hallId)
                    .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + hallId));
            ExhibitionHall exhibitionHall = new ExhibitionHall();
            exhibitionHall.setHall(hall);
            exhibitionHall.setExhibition(exhibition);
            exhibitionHallRepository.save(exhibitionHall);

            List<Stall> stalls = stallRepository.findByHall(hall);
            List<ExhibitionStall> exhibitionStalls = new ArrayList<>();
            for (Stall stall : stalls) {
                ExhibitionStall es = new ExhibitionStall();
                es.setExhibition(exhibition);
                es.setStall(stall);
                es.setBookingStatus(available);
                exhibitionStalls.add(es);
            }
            if (!exhibitionStalls.isEmpty()) {
                exhibitionStallRepository.saveAll(exhibitionStalls);
            }
        }
    }

    @Transactional
    public void upsertHallPrices(Exhibition exhibition, List<HallPriceDTO> hallPrices) {
        if (exhibition == null || hallPrices == null || hallPrices.isEmpty()) {
            return;
        }
        Map<Long, ExhibitionHall> exhibitionHallMap = exhibitionHallRepository.findByExhibition(exhibition).stream()
                .collect(Collectors.toMap(h -> h.getHall().getId(), h -> h));

        for (HallPriceDTO hallPriceDTO : hallPrices) {
            ExhibitionHall hall = exhibitionHallMap.get(hallPriceDTO.getHallId());
            if (hall == null) {
                throw new IllegalArgumentException("Hall not attached to exhibition: " + hallPriceDTO.getHallId());
            }
            StallType stallType = stallTypeRepository.findById(hallPriceDTO.getStallTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("StallType not found: " + hallPriceDTO.getStallTypeId()));
            ExhibitionHallPrice price = exhibitionHallPriceRepository
                    .findByExhibitionHallAndStallType(hall, stallType)
                    .orElseGet(() -> {
                        ExhibitionHallPrice ph = new ExhibitionHallPrice();
                        ph.setExhibitionHall(hall);
                        ph.setStallType(stallType);
                        return ph;
                    });
            price.setPrice(hallPriceDTO.getPrice());
            exhibitionHallPriceRepository.save(price);
        }
    }

    @Transactional
    public ExhibitionHallPrice createHallPrice(CreateExhibitionHallPriceRequest request) {
        ExhibitionHall hall = exhibitionHallRepository.findById(request.getExhibitionHallId())
                .orElseThrow(() -> new IllegalArgumentException("ExhibitionHall not found: " + request.getExhibitionHallId()));
        StallType stallType = stallTypeRepository.findById(request.getStallTypeId())
                .orElseThrow(() -> new IllegalArgumentException("StallType not found: " + request.getStallTypeId()));
        ExhibitionHallPrice price = new ExhibitionHallPrice();
        price.setExhibitionHall(hall);
        price.setStallType(stallType);
        price.setPrice(request.getPrice());
        return exhibitionHallPriceRepository.save(price);
    }

    @Transactional
    public ExhibitionHallPrice updateHallPrice(Long id, CreateExhibitionHallPriceRequest request) {
        ExhibitionHallPrice price = exhibitionHallPriceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExhibitionHallPrice not found: " + id));
        if (request.getExhibitionHallId() != null) {
        ExhibitionHall hall = exhibitionHallRepository.findById(request.getExhibitionHallId())
                    .orElseThrow(() -> new IllegalArgumentException("ExhibitionHall not found: " + request.getExhibitionHallId()));
            price.setExhibitionHall(hall);
        }
        if (request.getStallTypeId() != null) {
            StallType stallType = stallTypeRepository.findById(request.getStallTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("StallType not found: " + request.getStallTypeId()));
            price.setStallType(stallType);
        }
        if (request.getPrice() != null) {
            price.setPrice(request.getPrice());
        }
        return exhibitionHallPriceRepository.save(price);
    }

    public ExhibitionHallPriceResponse getHallPrice(Long id) {
        return toPriceResponse(exhibitionHallPriceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExhibitionHallPrice not found: " + id)));
    }

    public List<ExhibitionHallPriceResponse> getHallPricesByHall(Long hallId) {
        ExhibitionHall hall = exhibitionHallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("ExhibitionHall not found: " + hallId));
        return exhibitionHallPriceRepository.findByExhibitionHall(hall)
                .stream()
                .map(this::toPriceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHallPrice(Long id) {
        exhibitionHallPriceRepository.deleteById(id);
    }

    public List<StallSummaryResponse> getStallSummaries(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return stallRepository.findAllById(ids).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<StallStatusResponse> updateStallStatuses(UpdateStallStatusRequest request) {
        if (request == null || request.getStallIds() == null || request.getStallIds().isEmpty()) {
            return List.of();
        }
        BookingStatus status = null;
        if (request.getBookingStatus() != null) {
            try {
                status = BookingStatus.valueOf(request.getBookingStatus());
            } catch (IllegalArgumentException ignored) {
                // fall through, leave status null
            }
        }
        if (status == null) {
            throw new IllegalArgumentException("bookingStatus must be one of: " + java.util.Arrays.toString(BookingStatus.values()));
        }

        BookingStatus finalStatus = status;

        // First pass: detect conflicts to avoid partial updates
        List<Long> conflicted = new ArrayList<>();
        List<ExhibitionStall> targets = new ArrayList<>();
        for (Long stallId : request.getStallIds()) {
            List<ExhibitionStall> matches = exhibitionStallRepository.findByStallId_Id(stallId);
            targets.addAll(matches);
            if (finalStatus == BookingStatus.PENDING || finalStatus == BookingStatus.RESERVED) {
                for (ExhibitionStall es : matches) {
                    BookingStatus current = es.getBookingStatus();
                    if (current != null && current != BookingStatus.AVAILABLE) {
                        conflicted.add(es.getStall() != null ? es.getStall().getId() : es.getId());
                    }
                }
            }
        }

        if (!conflicted.isEmpty()) {
            throw new IllegalStateException("Stalls are not available: " + conflicted);
        }

        Map<Long, StallStatusResponse> updated = new HashMap<>();
        for (ExhibitionStall es : targets) {
            es.setBookingStatus(finalStatus);
            exhibitionStallRepository.save(es);
            updated.put(es.getId(), toStatus(es));
            publishStatusEvent(es);
        }
        return new java.util.ArrayList<>(updated.values());
    }

    private void publishStatusEvent(ExhibitionStall es) {
        if (es == null || es.getStall() == null || es.getStall().getHall() == null || es.getExhibition() == null) {
            return;
        }
        StallStatusChangedEvent event = StallStatusChangedEvent.builder()
                .exhibitionId(es.getExhibition().getId())
                .hallId(es.getStall().getHall().getId())
                .stallId(es.getStall().getId())
                .exhibitionStallId(es.getId())
                .status(es.getBookingStatus() != null ? es.getBookingStatus().name() : null)
                .reservationId(null)
                .build();
        stallStatusEventProducer.publish(event);
    }

    private HallSummaryResponse toHallSummary(Hall hall) {
        List<Stall> stalls = stallRepository.findByHall(hall);
        HallSummaryResponse dto = new HallSummaryResponse();
        dto.setId(hall.getId());
        dto.setHallName(hall.getHallName());
        dto.setTotalStalls(stalls.size());

        Map<Long, StallTypeCountResponse> counts = new java.util.LinkedHashMap<>();
        for (Stall s : stalls) {
            StallType type = s.getStallType();
            if (type == null) {
                continue;
            }
            counts.computeIfAbsent(type.getId(), k -> {
                StallTypeCountResponse c = new StallTypeCountResponse();
                c.setStallTypeId(type.getId());
                c.setStallType(type.getType());
                c.setCount(0);
                return c;
            });
            counts.get(type.getId()).setCount(counts.get(type.getId()).getCount() + 1);
        }
        dto.setStallTypes(new java.util.ArrayList<>(counts.values()));
        return dto;
    }

    private StallLayoutResponse toStallLayout(Stall stall) {
        StallLayoutResponse dto = new StallLayoutResponse();
        dto.setId(stall.getId());
        Optional.ofNullable(stall.getStallType()).ifPresent(type -> {
            dto.setStallTypeId(type.getId());
            dto.setStallType(type.getType());
        });
        dto.setPath(stall.getPath());
        if (stall.getPoints() != null) {
            dto.setPoints(stall.getPoints().stream()
                    .map(this::toPointDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private PointDto toPointDto(Point point) {
        PointDto dto = new PointDto();
        dto.setX(point.getX());
        dto.setY(point.getY());
        return dto;
    }

    private StallSummaryResponse toSummary(Stall stall) {
        StallSummaryResponse response = new StallSummaryResponse();
        response.setId(stall.getId());
        Optional.ofNullable(stall.getStallType()).ifPresent(type -> {
            response.setStallType(type.getType());
            priceFor(stall.getHall(), type).ifPresent(response::setPrice);
        });
        Optional.ofNullable(stall.getHall()).ifPresent(h -> response.setHallName(h.getHallName()));
        // derive status from exhibition stall association if present
        exhibitionStallRepository.findByStallId_Id(stall.getId()).stream().findFirst()
                .ifPresent(es -> {
                    response.setBookingStatus(
                            es.getBookingStatus() != null ? es.getBookingStatus().name() : null);
                    response.setStallName(es.getStallName());
                });
        return response;
    }

    private StallStatusResponse toStatus(ExhibitionStall stall) {
        StallStatusResponse response = new StallStatusResponse();
        response.setId(stall.getId());
        response.setBookingStatus(stall.getBookingStatus() != null ? stall.getBookingStatus().name() : null);
        return response;
    }

    private Optional<Long> priceFor(Hall hall, StallType type) {
        if (hall == null || type == null) return Optional.empty();
        return exhibitionHallPriceRepository.findFirstByExhibitionHall_Hall_IdAndStallType(hall.getId(), type)
                .map(ExhibitionHallPrice::getPrice);
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
