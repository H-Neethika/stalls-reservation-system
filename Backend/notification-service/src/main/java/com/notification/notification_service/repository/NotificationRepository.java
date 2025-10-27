package com.notification.notification_service.repository;

import com.notification.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    boolean existsByReservationId(Long reservationId);
    Optional<Notification> findByReservationId(Long reservationId);
}
