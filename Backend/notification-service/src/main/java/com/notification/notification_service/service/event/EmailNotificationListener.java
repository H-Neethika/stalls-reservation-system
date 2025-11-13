package com.notification.notification_service.service.event;

import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.service.EmailService;
import com.notification.notification_service.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);
    private final EmailService emailService;
    private final NotificationStatusService notificationStatusService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailNotification(EmailNotificationEvent event) {
        try {
            emailService.sendEmail(event);
            notificationStatusService.updateStatus(event.notificationId(), NotificationStatus.SENT, null);
        } catch (Exception e) {
            logger.error("Email send failed for {}: {}", event.to(), e.getMessage());
            notificationStatusService.updateStatus(event.notificationId(), NotificationStatus.FAILED, e.getMessage());
        }
    }
}