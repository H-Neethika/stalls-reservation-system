package com.booking.booking_service.service.maintenance;

import com.booking.booking_service.dto.request.UpdateStallStatusRequest;
import com.booking.booking_service.enums.ReservationStatus;
import com.booking.booking_service.model.Reservation;
import com.booking.booking_service.repository.ReservationRepository;
import com.booking.booking_service.service.ExhibitionServiceClient;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StallLockExpiryJob {

    private final ReservationRepository reservationRepository;
    private final ExhibitionServiceClient exhibitionServiceClient;

    @Value("${STALL_LOCK_TTL_MINUTES:10}")
    private long lockTtlMinutes;

    @Scheduled(fixedDelayString = "${STALL_LOCK_SWEEP_MS:60000}")
    @Transactional
    public void releaseExpiredLocks() {
        long now = System.currentTimeMillis();
        Date cutoff = new Date(now - lockTtlMinutes * 60_000);

        List<Reservation> expired = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING_PAYMENT, cutoff);

        if (expired.isEmpty()) {
            return;
        }

        log.info("Found {} reservations with expired payment window; releasing stalls", expired.size());
        for (Reservation reservation : expired) {
            List<Long> stallIds = reservation.getStallIds();
            if (stallIds != null && !stallIds.isEmpty()) {
                UpdateStallStatusRequest release = new UpdateStallStatusRequest();
                release.setStallIds(stallIds);
                release.setBookingStatus("AVAILABLE");
                try {
                    exhibitionServiceClient.updateBookingStatus(release);
                } catch (Exception ex) {
                    log.warn("Failed to release stalls for expired reservationId={}: {}", reservation.getId(), ex.getMessage());
                }
            }
            reservation.setStatus(ReservationStatus.FAILED);
            reservationRepository.save(reservation);
        }
    }
}
