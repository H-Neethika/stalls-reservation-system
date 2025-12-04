package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.dto.response.ExternalStallSummaryResponse;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.enums.ReservationStatus;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.dto.request.CreatePaymentRequest;
import com.booking.booking_service.dto.request.ReservationRequest;
import com.booking.booking_service.dto.response.PaymentIntentResponse;
import com.booking.booking_service.dto.response.ReservationResponse;
import com.booking.booking_service.dto.response.ReservedStallResponse;
import com.booking.booking_service.dto.request.UpdateStallStatusRequest;
import com.booking.booking_service.service.ExhibitionServiceClient;
import com.booking.booking_service.service.PaymentService;
import com.booking.booking_service.service.ReservationService;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationServiceImpl implements ReservationService {

  private final ReservationRepository reservationRepository;
  private final PaymentService paymentService;
  private final ExhibitionServiceClient exhibitionServiceClient;

  public ReservationServiceImpl(ReservationRepository reservationRepository,
                                PaymentService paymentService,
                                ExhibitionServiceClient exhibitionServiceClient) {
    this.reservationRepository = reservationRepository;
    this.paymentService = paymentService;
    this.exhibitionServiceClient = exhibitionServiceClient;
  }

  @Override
  public Reservation createReservation(ReservationRequest reservationRequest, Long userId) {
    List<Long> stallIds = reservationRequest.getStallIds();
    if (stallIds == null || stallIds.isEmpty()) {
      throw new IllegalArgumentException("At least one stall must be selected");
    }

    List<ExternalStallSummaryResponse> summaries = fetchStallSummaries(stallIds);
    if (summaries.size() != stallIds.size()) {
      Set<Long> returned = summaries.stream()
          .map(ExternalStallSummaryResponse::getId)
          .collect(Collectors.toSet());
      List<Long> missing = stallIds.stream()
          .filter(id -> !returned.contains(id))
          .toList();
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Unknown stall ids: " + missing
      );
    }

    // Block double-booking: ensure all requested stalls are available
    List<Long> unavailable = summaries.stream()
        .filter(s -> s.getBookingStatus() != null && !"AVAILABLE".equalsIgnoreCase(s.getBookingStatus()))
        .map(ExternalStallSummaryResponse::getId)
        .toList();
    List<Long> available = summaries.stream()
        .filter(s -> s.getBookingStatus() == null || "AVAILABLE".equalsIgnoreCase(s.getBookingStatus()))
        .map(ExternalStallSummaryResponse::getId)
        .toList();
    if (!unavailable.isEmpty()) {
        String reasonJson = String.format(
            "{\"detail\":\"Some requested stalls are not available\",\"errors\":{\"unavailable\":%s,\"available\":%s}}",
            unavailable,
            available
        );
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            reasonJson
        );
    }

    Long totalAmount = summaries.stream()
        .map(ExternalStallSummaryResponse::getPrice)
        .filter(p -> p != null)
        .reduce(0L, Long::sum);

    Reservation newReservation = new Reservation();
    newReservation.setUserId(userId);
    newReservation.setExhibitionId(reservationRequest.getExhibitionId());
    newReservation.setStallIds(stallIds);
    newReservation.setTotalAmount(totalAmount);
    newReservation.setCreatedAt(new Date());
    newReservation.setStatus(ReservationStatus.PENDING_PAYMENT);

    // mark stalls as PENDING to avoid race with other bookings
    UpdateStallStatusRequest statusRequest = new UpdateStallStatusRequest();
    statusRequest.setStallIds(stallIds);
    statusRequest.setBookingStatus("PENDING");
    try {
      exhibitionServiceClient.updateBookingStatus(statusRequest);
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Failed to lock stalls in exhibition-service",
          ex
      );
    }

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

  @Override
  public List<ReservationResponse> getReservationsForUser(Long userId) {
    if (userId == null) {
      return List.of();
    }
    return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public PaymentIntentResponse updateReservation(CreatePaymentRequest request) {
    Reservation reservation = reservationRepository.findById(request.getReservationId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Reservation not found with id " + request.getReservationId()));

    CreatePaymentRequest normalizedRequest = new CreatePaymentRequest(
        reservation.getId(),
        reservation.getTotalAmount() != null
            ? BigDecimal.valueOf(reservation.getTotalAmount())
            : BigDecimal.ZERO,
        request.getCurrency()
    );

    PaymentIntentResponse response = paymentService.createPaymentIntent(normalizedRequest);
    if (response == null) {
      throw new IllegalStateException("Failed to create payment intent");
    }
    return response;
  }


  //Convert Entity to DTO
  private ReservationResponse mapToResponse(Reservation reservation) {
    ReservationResponse dto = new ReservationResponse();
    dto.setId(reservation.getId());
    dto.setUserId(reservation.getUserId());
    dto.setExhibitionId(reservation.getExhibitionId());
    dto.setTotalAmount(reservation.getTotalAmount());
    dto.setCreatedAt(reservation.getCreatedAt());
    dto.setStatus(reservation.getStatus() != null ? reservation.getStatus().name() : null);

    List<ExternalStallSummaryResponse> summaries = fetchStallSummaries(reservation.getStallIds());

    List<ReservedStallResponse> stalls = summaries.stream()
            .map(st -> {
              ReservedStallResponse stallDto = new ReservedStallResponse();
              stallDto.setId(st.getId());
              stallDto.setDisplayName(st.getDisplayName());
              stallDto.setPrice(st.getPrice());
              stallDto.setStallType(st.getStallType());
              stallDto.setHallName(st.getHallName());
              stallDto.setStallName(st.getStallName());
              stallDto.setBookingStatus(st.getBookingStatus() != null
                  ? st.getBookingStatus()
                  : "RESERVED");
              return stallDto;
            })
            .collect(Collectors.toList());

    dto.setStalls(stalls);
    return dto;
  }

  private List<ExternalStallSummaryResponse> fetchStallSummaries(List<Long> stallIds) {
    if (stallIds == null || stallIds.isEmpty()) {
      return List.of();
    }
    List<ExternalStallSummaryResponse> response = exhibitionServiceClient.getStallSummaries(stallIds);
    return response != null ? response : List.of();
  }
}
