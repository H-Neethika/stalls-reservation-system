package com.notification.notification_service.repository;

import com.notification.notification_service.enums.NotificationType;
import com.notification.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<List<Notification>> findAllByRecipientEmail(String recipientEmail);
    Optional<List<Notification>> findAllByUserId(Long userId);
}
