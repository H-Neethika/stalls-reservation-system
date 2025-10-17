package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.dto.HallPriceDto;
import com.booking.booking_service.dto.PriceDto;
import com.booking.booking_service.model.BookingStatus;
import com.booking.booking_service.model.ExhibitionStall;
import com.booking.booking_service.model.Genre;
import com.booking.booking_service.model.Stall;
import com.booking.booking_service.repository.BookingStatusRepository;
import com.booking.booking_service.repository.ExhibitionStallRepository;
import com.booking.booking_service.request.CreateExhibitionStallRequest;
import com.booking.booking_service.service.ExhibitionStallService;
import com.booking.booking_service.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExhibitionStallServiceImpl implements ExhibitionStallService {

    @Autowired
    private ExhibitionStallRepository exhibitionStallRepository;
    @Autowired
    private StallService stallService;
    @Autowired
    private BookingStatusRepository bookingStatusRepository;


    @Override
    public Page<ExhibitionStall> getExhibitionStall(Long hallId, String bookingStatus, String stallType, String genre, Long exhibitionId, Pageable pageable) {

        return exhibitionStallRepository.findAllByFilters(hallId, bookingStatus, stallType, genre, exhibitionId, pageable);
    }

    @Override
    public List<ExhibitionStall> createExhibitionStall(CreateExhibitionStallRequest exhibitionStallReq) {

        List<HallPriceDto> hallPriceList = exhibitionStallReq.getHallPriceList();
        List<ExhibitionStall> exhibitionStalls = new ArrayList<>();
        Map<Long, HallPriceDto> hallPriceMap = hallPriceList.stream()
                .collect(Collectors.toMap(HallPriceDto::getHallId, dto -> dto));

        List<Long> hallIds = new ArrayList<>();
        for (HallPriceDto hallPrice : hallPriceList) {
            hallIds.add(hallPrice.getHallId());
        }
        Long exhibitionId = exhibitionStallReq.getExhibitionId();

        for (Long hallId : hallIds) {
            List<Stall> stalls = stallService.findAllByHallId(hallId);
            HallPriceDto hallPrice = hallPriceMap.get(hallId);
            for (Stall stall : stalls) {

                //Check for existing record
                boolean exists = exhibitionStallRepository.existsByExhibitionIdAndStallId(exhibitionId, stall.getId());
                if (exists) {
                    // skip if already created
                    continue;
                }
                ExhibitionStall exhibitionStall = new ExhibitionStall();
                exhibitionStall.setExhibitionId(exhibitionId);
                exhibitionStall.setStallId(stall.getId());
                exhibitionStall.setStallType(stall.getStallType());
                exhibitionStall.setHallId(hallId);

                Optional<BookingStatus> bookingStatus = bookingStatusRepository.findById(1L);
                exhibitionStall.setBookingStatus(bookingStatus.get());
                exhibitionStall.setGenres(new ArrayList<>());
                List<PriceDto> priceList = hallPrice.getPriceList();
                for (PriceDto price : priceList) {
                    if (price.getStallType().equals(stall.getStallType())) {
                        exhibitionStall.setPrice(price.getPrice());
                        break; // stop once matched
                    }
                }

                ExhibitionStall savedExhibitionStall = exhibitionStallRepository.save(exhibitionStall);
                exhibitionStalls.add(savedExhibitionStall);

            }

        }

        return exhibitionStalls;
    }

    @Override
    public ExhibitionStall updateExhibitionStall(Long stallId, Long exhibitionId, ExhibitionStall updatedStall) {

        ExhibitionStall foundExhibitionStall = exhibitionStallRepository.findByExhibitionIdAndStallId(exhibitionId, stallId).orElseThrow(() -> new RuntimeException("ExhibitionStall not found for the exhibitionId : " + exhibitionId + " and stallId: " + stallId));
        if (updatedStall.getPrice() != null) {
            foundExhibitionStall.setPrice(updatedStall.getPrice());
        }

        if (updatedStall.getStallType() != null) {
            foundExhibitionStall.setStallType(updatedStall.getStallType());
        }

        if (updatedStall.getGenres() != null) {
            foundExhibitionStall.setGenres(updatedStall.getGenres());
        }

        if (updatedStall.getBookingStatus() != null) {
            foundExhibitionStall.setBookingStatus(updatedStall.getBookingStatus());
        }
        return exhibitionStallRepository.save(foundExhibitionStall);
    }

    @Override
    public void deleteExhibitionStall(Long stallId, Long exhibitionId) {
        ExhibitionStall foundExhibitionStall = exhibitionStallRepository.findByExhibitionIdAndStallId(exhibitionId, stallId).orElseThrow(() -> new RuntimeException("ExhibitionStall not found for the exhibitionId : " + exhibitionId + " and stallId: " + stallId));
        exhibitionStallRepository.delete(foundExhibitionStall);
    }


    @Override
    public List<Genre> getExhibitionStallGenres(Long hallId, Long stallId) {
        if (hallId != null) {
            List<ExhibitionStall> stalls = exhibitionStallRepository.findByHallId(hallId);
            return stalls.stream().flatMap(e -> e.getGenres().stream()).distinct().collect(Collectors.toList());
        }

        if (stallId != null) {
            return exhibitionStallRepository.findByStallId(stallId).map(ExhibitionStall::getGenres).orElseThrow(() -> new RuntimeException("No ExhibitionStall found in this stallId : " + stallId));
        }

        throw new IllegalArgumentException("Either hallId or stallId must be provided");


    }
}
