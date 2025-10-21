package com.notification.notification_service.service;

import com.notification.notification_service.dto.BookingEvent;
import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private EmailService emailService;

    public Notification getNotificationDetails(Long reservationId) {
        return notificationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found for reservationId: " + reservationId));
    }

    @Transactional
    public void processNotification(BookingEvent event) {
        if (notificationRepository.existsByReservationId(event.getReservationId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setReservationId(event.getReservationId());
        notification.setRecipientEmail(event.getUserEmail());
        notification = notificationRepository.save(notification);

        try {
            byte[] qrCode = qrCodeService.generateQRCode(event.getReservationId().toString(), 250, 250);
            sendConfirmationEmail(event, qrCode);
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastError(e.getMessage());
        } finally {
            notificationRepository.save(notification);
        }
    }

    /*@Retryable(
            value = { MessagingException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000) // 2 seconds delay between retries
    )*/
    private void sendConfirmationEmail(BookingEvent event, byte[] qrCode) throws MessagingException {
        String subject = "Your Reservation is Confirmed!";
        String body = String.format("<h1>Hi %s,</h1><p>Your reservation (ID: %s) is confirmed. " +
                        "Please find your QR code attached.</p>",
                event.getUserName(), event.getReservationId());

        emailService.sendEmailWithAttachment(
                event.getUserEmail(),
                subject,
                body,
                qrCode,
                "reservation-qr.png"
        );
    }

    public byte[] getQRCode(String reservationId) {
        return qrCodeService.generateQRCode(reservationId, 250, 250);
    }
}
