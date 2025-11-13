package com.notification.notification_service.service;

import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.exception.NotificationNotFoundException;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationStatusService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStatusService.class);

    private final NotificationRepository notificationRepository;

    @Transactional
    public void updateStatus(UUID id, NotificationStatus status, String errorMessage) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));

        notification.setStatus(status);
        notification.setLastError(errorMessage);

        logger.info("Updated notification [{}] status to [{}] with errorMessage [{}]", id, status, errorMessage);
    }
}