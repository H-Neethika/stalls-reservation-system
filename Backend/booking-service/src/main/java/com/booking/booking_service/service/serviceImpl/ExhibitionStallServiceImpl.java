package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.dto.response.ExhibitionDTO;
import com.booking.booking_service.dto.response.ExternalStallSummaryResponse;
import com.booking.booking_service.dto.response.ReservedStallDto;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.dto.response.PaymentSuccessResponse;
import com.booking.booking_service.dto.response.UserResponse;
import com.booking.booking_service.service.ExhibitionServiceClient;
import com.booking.booking_service.service.ExhibitionStallService;
import com.booking.booking_service.service.UserService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExhibitionStallServiceImpl implements ExhibitionStallService {

  private final ReservationRepository reservationRepository;
  private final ExhibitionServiceClient exhibitionServiceClient;
  private final UserService userService;

  @Override
  @Transactional(readOnly = true)
  public PaymentSuccessResponse updateStallBookingStatus(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Reservation not found with id " + reservationId));

    // Fetch user details for notification payload
    UserResponse user;
    try {
      user = userService.getUserById(reservation.getUserId());
    } catch (Exception ex) {
      log.error("Unable to fetch user {} for reservation {}: {}", reservation.getUserId(), reservationId, ex.getMessage());
      throw new IllegalStateException("User details required for reservation notification", ex);
    }
    if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
      log.error("User {} for reservation {} has no email; cannot send notification", reservation.getUserId(), reservationId);
      throw new IllegalStateException("User email required for reservation notification");
    }

    // Fetch stall summaries to include in the success response
    List<ExternalStallSummaryResponse> stalls = Optional.ofNullable(
            exhibitionServiceClient.getStallSummaries(reservation.getStallIds()))
        .orElse(List.of());

    List<ReservedStallDto> reservedStallDtos = stalls.stream()
        .map(stall -> {
          ReservedStallDto dto = new ReservedStallDto();
          dto.setStallName(Optional.ofNullable(stall.getStallName()).orElse("Stall " + stall.getId()));
          dto.setStallType(Optional.ofNullable(stall.getStallType()).orElse("N/A"));
          dto.setHallName(Optional.ofNullable(stall.getHallName()).orElse("N/A"));
          return dto;
        })
        .collect(Collectors.toList());

    PaymentSuccessResponse response = new PaymentSuccessResponse();
    response.setReservationId(reservationId);
    response.setUserId(reservation.getUserId());
    response.setUsername(user.getName());
    response.setEmail(user.getEmail());

    ExhibitionDTO exhibitionDTO = exhibitionServiceClient.getExhibition(reservation.getExhibitionId())
        .getBody();
    LocalDateTime startDateTime = exhibitionDTO.getStartDateTime();
    LocalDateTime endDateTime = exhibitionDTO.getEndDateTime();
//    Date eventDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
    // treat the startDateTime as "local event time" without converting to system timezone
    response.setEventStartDateTime(Date.from(startDateTime.atZone(ZoneId.of("UTC")).toInstant()));
    response.setEventEndDateTime(Date.from(endDateTime.atZone(ZoneId.of("UTC")).toInstant()));

    response.setFairName(exhibitionDTO.getExhibitionName());
    response.setStalls(reservedStallDtos);
    response.setBookingDateTime(new Date());

    // TODO: Call exhibition-service to mark stalls as RESERVED after payment, if an endpoint exists.
    return response;
  }
}
