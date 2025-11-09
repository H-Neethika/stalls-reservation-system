package com.notification.notification_service.service;

import com.notification.notification_service.dto.BookingEvent;
import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
            byte[] qrCode = qrCodeService.generateQRCode(event.getReservationId().toString(), 300, 300);
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
        String fairName ="Colombo International Book Fair";
        Long reservationId = event.getReservationId();
        String stallName = "Stall B-12";
        LocalDateTime fairDate = LocalDateTime.now();

        String htmlBody = """
                <html>
                <head>
                    <style>
                        body {
                            font-family: 'Segoe UI', Arial, sans-serif;
                            background-color: #f8f9fa;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            background: #ffffff;
                            margin: 30px auto;
                            border-radius: 12px;
                            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background-color: #5b3cc4;
                            color: #fff;
                            text-align: center;
                            padding: 25px 20px;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            padding: 30px 40px;
                            text-align: left;
                        }
                        .content h2 {
                            color: #5b3cc4;
                        }
                        .details {
                            margin-top: 20px;
                            border-top: 1px solid #ddd;
                            padding-top: 15px;
                            line-height: 1.6;
                        }
                        .qr-section {
                            text-align: center;
                            margin-top: 30px;
                        }
                        .qr-section p {
                            font-size: 14px;
                            color: #777;
                        }
                        .footer {
                            background: #f1f1f1;
                            text-align: center;
                            padding: 15px;
                            font-size: 13px;
                            color: #666;
                        }
                        .btn {
                             display: inline-block;
                             background-color: #5b3cc4;
                             color: white;
                             padding: 10px 20px;
                             border-radius: 5px;
                             text-decoration: none;
                             margin-top: 15px;
                         }
                         .btn:hover {
                             background-color: #4a2aa5;
                             color: white;
                         }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📖 %s - Stall Reservation Confirmed!</h1>
                        </div>
                        <div class="content">
                            <h2>Dear Exhibitor,</h2>
                            <p>We’re thrilled to confirm your stall reservation for the upcoming <b>%s</b>.</p>
                            <div class="details">
                                <p><b>Reservation ID:</b> %s</p>
                                <p><b>Stall Name:</b> %s</p>
                                <p><b>Fair Date:</b> %s</p>
                            </div>

                            <div class="qr-section">
                                <p>Please present the attached QR code at the venue entrance for verification.</p>
                                <p>We look forward to seeing you at the fair!</p>
                                <a href="#" class="btn">View Event Details</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2025 Book Fair Committee | <a href="https://bookfair.com" style="color:#5b3cc4;text-decoration:none;">Visit Website</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(fairName, fairName, reservationId.toString(), stallName, fairDate.toLocalDate().toString());

        emailService.sendStallReservationConfirmation(
                event.getUserEmail(),
                fairName,
                htmlBody,
                qrCode
        );

    }

    public byte[] getQRCode(String reservationId) {
        return qrCodeService.generateQRCode(reservationId, 250, 250);
    }
}
