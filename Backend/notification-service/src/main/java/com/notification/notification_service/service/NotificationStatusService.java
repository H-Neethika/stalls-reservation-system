package com.notification.notification_service.service;

import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationStatusService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void updateStatus(UUID id, NotificationStatus status, String errorMessage) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setStatus(status);
            notification.setLastError(errorMessage);
            notificationRepository.save(notification);
        }
    }
}
