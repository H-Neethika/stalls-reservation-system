package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.dto.ExternalStallSummary;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.request.ReservationRequest;
import com.booking.booking_service.response.ReservationResponse;
import com.booking.booking_service.response.ReservedStallResponse;
import com.booking.booking_service.service.ReservationService;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReservationServiceImpl implements ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${EXHIBITION_SERVICE_BASE_URL:http://localhost:8082}")
  private String exhibitionServiceBaseUrl;


  @Override
  public Reservation createReservation(ReservationRequest reservationRequest) {
    List<Long> stallIds = reservationRequest.getStallIds();
    List<ExternalStallSummary> summaries = fetchStallSummaries(stallIds);

    Long totalAmount = summaries.stream()
            .map(ExternalStallSummary::getPrice)
            .filter(p -> p != null)
            .reduce(0L, Long::sum);

    Reservation newReservation = new Reservation();
    newReservation.setUserId(reservationRequest.getUserId());
    newReservation.setExhibitionId(reservationRequest.getExhibitionId());
    newReservation.setStallIds(stallIds);
    newReservation.setTotalAmount(totalAmount);
    newReservation.setCreatedAt(new Date());

    return reservationRepository.save(newReservation);
  }

  @Override
  public ReservationResponse getReservationById(Long id) throws Exception {
    Optional<Reservation> reservation = reservationRepository.findById(id);
    if (reservation.isEmpty()) {
      throw new Exception("Reservation not found with id " + id);
    }
    return mapToResponse(reservation.get());
  }

  @Override
  public List<ReservationResponse> getAllReservation() {
    return reservationRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  //Convert Entity to DTO
  private ReservationResponse mapToResponse(Reservation reservation) {
    ReservationResponse dto = new ReservationResponse();
    dto.setId(reservation.getId());
    dto.setUserId(reservation.getUserId());
    dto.setExhibitionId(reservation.getExhibitionId());
    dto.setTotalAmount(reservation.getTotalAmount());
    dto.setCreatedAt(reservation.getCreatedAt());

    List<ExternalStallSummary> summaries = fetchStallSummaries(reservation.getStallIds());

    List<ReservedStallResponse> stalls = summaries.stream()
            .map(st -> {
              ReservedStallResponse stallDto = new ReservedStallResponse();
              stallDto.setId(st.getId());
              stallDto.setPrice(st.getPrice());
              stallDto.setStallType(st.getStallType());
              stallDto.setHallName(st.getHallName());
              stallDto.setBookingStatus("RESERVED");
              return stallDto;
            })
            .collect(Collectors.toList());

    dto.setStalls(stalls);
    return dto;
  }

  private List<ExternalStallSummary> fetchStallSummaries(List<Long> stallIds) {
    if (stallIds == null || stallIds.isEmpty()) {
      return List.of();
    }
    String joined = stallIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    String encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8);
    URI uri = URI.create(exhibitionServiceBaseUrl + "/api/layout/stalls/summary?ids=" + encoded);
    ResponseEntity<ExternalStallSummary[]> response = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            HttpEntity.EMPTY,
            ExternalStallSummary[].class);
    ExternalStallSummary[] body = response.getBody();
    if (body == null) {
      return List.of();
    }
    return Arrays.asList(body);
  }
}
