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
import java.util.ArrayList;
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

    public Hall createHall(String hallName) {
        Hall hall = new Hall();
        hall.setHallName(hallName);
        return hallRepository.save(hall);
    }

    public List<Hall> getHalls() {
        return hallRepository.findAll();
    }

    public List<HallSummaryResponse> getHallSummaries() {
        return hallRepository.findAll().stream()
                .map(this::toHallSummary)
                .collect(Collectors.toList());
    }

    public List<StallType> getStallTypes() {
        return stallTypeRepository.findAll();
    }

    @Transactional
    public ExhibitionHall createExhibitionHall(CreateExhibitionHallRequest request) {
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + request.getHallId()));
        Exhibition exhibition = exhibitionRepository.findById(request.getExhibitionId())
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + request.getExhibitionId()));

        ExhibitionHall exhibitionHall = new ExhibitionHall();
        exhibitionHall.setHall(hall);
        exhibitionHall.setExhibition(exhibition);
        return exhibitionHallRepository.save(exhibitionHall);
    }

    public List<ExhibitionHall> getExhibitionHalls(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + exhibitionId));
        return exhibitionHallRepository.findByExhibition(exhibition);
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
    public StallType createStallType(CreateStallTypeRequest request) {
        StallType stallType = new StallType();
        stallType.setType(request.getType());
        return stallTypeRepository.save(stallType);
    }

    public List<ExhibitionHallPriceResponse> getStallTypes(Long exhibitionHallId) {
        ExhibitionHall exhibitionHall = exhibitionHallRepository.findById(exhibitionHallId)
                .orElseThrow(() -> new IllegalArgumentException("ExhibitionHall not found: " + exhibitionHallId));
        return exhibitionHallPriceRepository.findByExhibitionHall(exhibitionHall)
                .stream()
                .map(this::toPriceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Stall createStall(CreateStallRequest request) {
        StallType stallType = stallTypeRepository.findById(request.getStallTypeId())
                .orElseThrow(() -> new IllegalArgumentException("StallType not found: " + request.getStallTypeId()));
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + request.getHallId()));

        Stall stall = new Stall();
        stall.setHall(hall);
        stall.setStallType(stallType);
        stall.setPath(request.getPath());
        if (request.getPoints() != null) {
            List<Point> pts = request.getPoints().stream()
                    .map(this::toPoint)
                    .collect(Collectors.toList());
            stall.setPoints(pts);
        }
        return stallRepository.save(stall);
    }

    public List<Stall> getStallsByHall(Long hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + hallId));
        return stallRepository.findByHall(hall);
    }

    public HallDetailsResponse getHallDetails(Long hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + hallId));
        List<Stall> stalls = stallRepository.findByHall(hall);
        HallDetailsResponse dto = new HallDetailsResponse();
        dto.setHallId(hall.getId());
        dto.setHallName(hall.getHallName());
        dto.setStalls(stalls.stream().map(this::toDetails).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public ExhibitionStall assignStallToExhibition(AssignStallRequest request) {
        Stall stall = stallRepository.findById(request.getStallId())
                .orElseThrow(() -> new IllegalArgumentException("Stall not found: " + request.getStallId()));
        Exhibition exhibition = exhibitionRepository.findById(request.getExhibitionId())
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + request.getExhibitionId()));

        ExhibitionStall exhibitionStall = new ExhibitionStall();
        exhibitionStall.setStall(stall);
        exhibitionStall.setExhibition(exhibition);
        return exhibitionStallRepository.save(exhibitionStall);
    }

    public List<ExhibitionStall> getStallsForExhibition(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + exhibitionId));
        return exhibitionStallRepository.findByExhibition(exhibition);
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
    public List<StallStatusResponse> reserveStalls(Long exhibitionId, UpdateStallStatusRequest request) {
        return updateStatus(exhibitionId, request, BookingStatus.RESERVED);
    }

    @Transactional
    public List<StallStatusResponse> releaseStalls(Long exhibitionId, UpdateStallStatusRequest request) {
        return updateStatus(exhibitionId, request, BookingStatus.AVAILABLE);
    }

    public List<StallStatusResponse> getStatuses(Long exhibitionId, List<Long> stallIds) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + exhibitionId));
        List<ExhibitionStall> stalls = exhibitionStallRepository.findByIdInAndExhibition(stallIds, exhibition);
        return stalls.stream().map(this::toStatus).collect(Collectors.toList());
    }

    private List<StallStatusResponse> updateStatus(Long exhibitionId, UpdateStallStatusRequest request, BookingStatus status) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("Exhibition not found: " + exhibitionId));
        if (request.getStallIds() == null || request.getStallIds().isEmpty()) {
            throw new IllegalArgumentException("stallIds cannot be empty");
        }
        List<ExhibitionStall> stalls = exhibitionStallRepository.findByIdInAndExhibition(request.getStallIds(), exhibition);
        if (stalls.size() != request.getStallIds().size()) {
            throw new IllegalArgumentException("Some stalls not found for exhibition " + exhibitionId);
        }
        stalls.forEach(s -> s.setBookingStatus(status));
        exhibitionStallRepository.saveAll(stalls);
        return stalls.stream().map(this::toStatus).collect(Collectors.toList());
    }

    private Point toPoint(PointDto dto) {
        Point point = new Point();
        point.setX(dto.getX());
        point.setY(dto.getY());
        return point;
    }

    private StallDetailsResponse toDetails(Stall stall) {
        StallDetailsResponse dto = new StallDetailsResponse();
        dto.setId(stall.getId());
        if (stall.getStallType() != null) {
            dto.setStallTypeId(stall.getStallType().getId());
            dto.setStallType(stall.getStallType().getType());
        }
        dto.setPath(stall.getPath());
        if (stall.getPoints() != null) {
            dto.setPoints(stall.getPoints().stream().map(p -> {
                PointDto pd = new PointDto();
                pd.setX(p.getX());
                pd.setY(p.getY());
                return pd;
            }).collect(Collectors.toList()));
        }
        return dto;
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
                .ifPresent(es -> response.setBookingStatus(
                        es.getBookingStatus() != null ? es.getBookingStatus().name() : null));
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
