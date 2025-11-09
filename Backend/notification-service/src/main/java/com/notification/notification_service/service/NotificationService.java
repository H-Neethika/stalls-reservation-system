package com.notification.notification_service.service;

import com.notification.notification_service.dto.BookingEvent;
import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Base64;

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
    public void processNotification(BookingEvent bookingEvent) {
        if (notificationRepository.existsByReservationId(bookingEvent.getReservationId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setReservationId(bookingEvent.getReservationId());
        notification.setRecipientEmail(bookingEvent.getUserEmail());
        notification = notificationRepository.save(notification);

        try {
            byte[] qrCode = qrCodeService.generateQRCode(bookingEvent.getReservationId().toString(), 500, 500);
            sendConfirmationEmail(bookingEvent, qrCode);
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
    private void sendConfirmationEmail(BookingEvent bookingEvent, byte[] qrCode) throws MessagingException {
        String fairName = bookingEvent.getFairName();
        Long reservationId = bookingEvent.getReservationId();
        String stallName = bookingEvent.getStallName();
        LocalDateTime bookingTime = bookingEvent.getBookingTime();
        LocalDateTime eventTime = bookingEvent.getEventTime();
        URI websiteLink = bookingEvent.getWebsiteLink();
        URI eventLink = bookingEvent.getEventLink();

        // ✅ Convert QR Code to Base64 for embedding
        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCode);

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
                .qr-section img {
                    width: 180px;
                    height: 180px;
                    margin-top: 10px;
                    border: 4px solid #eee;
                    border-radius: 10px;
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
                    <h2>Dear %s,</h2>
                    <p>We’re thrilled to confirm your stall reservation for the upcoming <b>%s</b>.</p>
                    <div class="details">
                        <p><b>Reservation ID:</b> %s</p>
                        <p><b>Stall Name:</b> %s</p>
                        <p><b>Booking Time:</b> %s</p>
                        <p><b>Event Date & Time:</b> %s</p>
                    </div>

                    <div class="qr-section">
                        <p>Please present this QR code at the venue entrance for verification:</p>
                        <img src="cid:qrCode" alt="QR Code" style="width:180px;height:180px;border:4px solid #eee;border-radius:10px;" />
                        <p>We look forward to seeing you at the fair!</p>
                        <a href="%s" class="btn">View Event Details</a>
                    </div>
                </div>
                <div class="footer">
                    <p>© 2025 Book Fair Committee |
                    <a href="%s" style="color:#5b3cc4;text-decoration:none;">Visit Website</a></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                fairName,
                bookingEvent.getUserName(),
                fairName,
                reservationId,
                stallName,
                bookingTime.toLocalDate().toString() + " " + bookingTime.toLocalTime().toString(),
                eventTime.toLocalDate().toString() + " " + eventTime.toLocalTime().toString(),
                eventLink.toString(),
                websiteLink.toString()
        );

        // ✅ Send the HTML email with QR code
        emailService.sendStallReservationConfirmation(
                bookingEvent.getUserEmail(),
                fairName + " - Reservation Confirmation",
                htmlBody,
                qrCode
        );
    }



    public byte[] getQRCode(String reservationId) {
        return qrCodeService.generateQRCode(reservationId, 250, 250);
    }
}
