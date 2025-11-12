package com.notification.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.notification_service.dto.AccountActivationNotificationRequest;
import com.notification.notification_service.dto.ReservationNotificationRequest;
import com.notification.notification_service.enums.EmailAttachmentType;
import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.enums.NotificationType;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.model.email_details.AccountActivationEmailDetails;
import com.notification.notification_service.model.email_details.EmailDetails;
import com.notification.notification_service.model.email_details.ReservationEmailDetails;
import com.notification.notification_service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private EmailService emailService;

    @Value("${OFFICIAL_WEBSITE_LINK}")
    private String websiteLink;

    @Value("${NOTIFICATION_QRCODE_SECRET}")
    private String base64Secret;

    public Optional<Notification> getReservationNotification(Long reservationId, Long userId) {
        return notificationRepository.findAllByUserId(userId)
                .orElse(List.of()) // safely unwrap Optional or use empty list
                .stream()
                .filter(notification -> {
                    EmailDetails details = notification.getEmailDetails();
                    if (details instanceof ReservationEmailDetails reservationDetails) {
                        return reservationDetails.getReservationId().equals(reservationId);
                    }
                    return false;
                })
                .findFirst();
    }

    public Optional<Notification> getAccountCreationNotification(Long userId) {
        return  notificationRepository.findAllByUserId(userId)
                .orElse(List.of())
                .stream()
                .filter(notification -> {
                    EmailDetails details = notification.getEmailDetails();
                    return details instanceof AccountActivationEmailDetails;
                })
                .findFirst();
    }

    public byte[] createQRCode(String qrcodeDetails, int width, int height) {
        String encrypted = AESEncryptor.encrypt(qrcodeDetails, this.base64Secret);
        return qrCodeService.generateQRCode(encrypted, width, height);
    }

    public String getQRCodeDetails(String qrcodeSecret) {
        return AESEncryptor.decrypt(qrcodeSecret, this.base64Secret);
    }

    @Transactional
    public void sendAccountCreationEmail(AccountActivationNotificationRequest notificationRequest) {
        AccountActivationEmailDetails accountCreationEmailDetails = new AccountActivationEmailDetails(
                notificationRequest.getCreatedTime(),
                notificationRequest.getRole(),
                notificationRequest.getLoginLink()
        );
        accountCreationEmailDetails.setUserName(notificationRequest.getUserName());

        String htmlBody = getAccountActivationEmailBody(accountCreationEmailDetails);
        String email = notificationRequest.getEmail();
        Optional<Notification> optionalNotification = getAccountCreationNotification(notificationRequest.getUserId());

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (notification.getStatus().equals(NotificationStatus.FAILED) || notification.getStatus().equals(NotificationStatus.PENDING)) {
                emailService.sendEmail(
                        null,
                        email,
                        "Welcome to the Book Fair Platform!",
                        htmlBody,
                        true,
                        null,
                        null,
                        null
                );
                return;
            }
            throw new IllegalArgumentException("An account creation email has already been sent to " + email);
        } else {
            Notification notification = new Notification();
            notification.setNotificationType(NotificationType.ACCOUNT_ACTIVATION);
            notification.setRecipientEmail(email);
            notification.setUserId(notificationRequest.getUserId());
            notification.setEmailDetails(accountCreationEmailDetails);
            notificationRepository.save(notification);

            emailService.sendEmail(
                    null,
                    email,
                    "Welcome to the Book Fair Platform!",
                    htmlBody,
                    true,
                    null,
                    null,
                    null
            );
        }
    }

    @Transactional
    public void resendAccountCreationEmail(Long userId) {
        Optional<Notification> notificationOptional = getAccountCreationNotification(userId);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            if (notification.getStatus().equals(NotificationStatus.SENT)) {
                throw new IllegalArgumentException("The account creation email has already been sent.");
            }
            AccountActivationNotificationRequest accountActivationNotificationRequest = getAccountActivationNotificationRequest(notification);
            sendAccountCreationEmail(accountActivationNotificationRequest);
        } else {
            throw new IllegalArgumentException("There is no account creation notification found.");
        }
    }

    private static AccountActivationNotificationRequest getAccountActivationNotificationRequest(Notification notification) {
        AccountActivationEmailDetails accountActivationEmailDetails = (AccountActivationEmailDetails) notification.getEmailDetails();
        return new AccountActivationNotificationRequest(
                notification.getUserId(),
                notification.getRecipientEmail(),
                accountActivationEmailDetails.getUserName(),
                accountActivationEmailDetails.getCreatedTime(),
                accountActivationEmailDetails.getRole(),
                accountActivationEmailDetails.getLoginLink()
        );
    }

    @Transactional
    public void sendReservationConfirmationEmail(ReservationNotificationRequest notificationRequest) throws JsonProcessingException {
        ReservationEmailDetails reservationEmailDetails = getReservationEmailDetails(notificationRequest);

        String fairName = reservationEmailDetails.getFairName();
        Long reservationId = reservationEmailDetails.getReservationId();
        String stallName = reservationEmailDetails.getStallName();
        String stallSize = reservationEmailDetails.getStallSize();
        LocalDateTime bookingTime = reservationEmailDetails.getBookingTime();
        LocalDateTime eventTime = reservationEmailDetails.getEventTime();
        URI websiteUri = URI.create(this.websiteLink);
        URI eventLink = reservationEmailDetails.getEventLink();
        String email = notificationRequest.getEmail();

        Map<String, Object> qrcodeDetails = new HashMap<>();
        qrcodeDetails.put("reservationId", reservationEmailDetails.getReservationId());
        qrcodeDetails.put("stallName", reservationEmailDetails.getStallName());
        qrcodeDetails.put("fairName", reservationEmailDetails.getFairName());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(qrcodeDetails);
        byte[] qrCodeBytes = createQRCode(jsonString, 500, 500);

        String htmlBody = getReservationConfirmationHTMLBody(
                reservationEmailDetails,
                fairName,
                reservationId,
                stallName,
                bookingTime,
                eventTime,
                eventLink,
                websiteUri
        );

        Optional<Notification> existNotification = getReservationNotification(reservationId, notificationRequest.getUserId());
        if (existNotification.isPresent()) {
            if (existNotification.get().getStatus().equals(NotificationStatus.PENDING) || existNotification.get().getStatus().equals(NotificationStatus.FAILED)) {
                // Resend the existing notification
                emailService.sendEmail(
                        existNotification.get().getId(),
                        email,
                        fairName + " - Reservation Confirmation",
                        htmlBody,
                        true,
                        "reservation_qr.png",
                        EmailAttachmentType.IMAGE,
                        qrCodeBytes
                );
                existNotification.get().setStatus(NotificationStatus.PENDING);
                return;
            }
            throw new IllegalArgumentException("The reservation with ID " + reservationId + " has already been confirmed.");
        } else {
            Notification notification = new Notification();
            notification.setRecipientEmail(email);
            notification.setNotificationType(NotificationType.STALL_RESERVATION);
            notification.setUserId(notificationRequest.getUserId());
            EmailDetails newEmailDetails = new ReservationEmailDetails(
                    reservationId,
                    fairName,
                    stallName,
                    stallSize,
                    bookingTime,
                    eventTime,
                    eventLink
            );
            newEmailDetails.setUserName(reservationEmailDetails.getUserName());
            notification.setEmailDetails(newEmailDetails);

            Notification savedNotification = notificationRepository.save(notification);

            emailService.sendEmail(
                    savedNotification.getId(),
                    email,
                    fairName + " - Reservation Confirmation",
                    htmlBody,
                    true,
                    "reservation_qr.png",
                    EmailAttachmentType.IMAGE,
                    qrCodeBytes
            );
        }
    }

    private static ReservationEmailDetails getReservationEmailDetails(ReservationNotificationRequest notificationRequest) {
        ReservationEmailDetails reservationEmailDetails = new  ReservationEmailDetails(
                notificationRequest.getReservationId(),
                notificationRequest.getFairName(),
                notificationRequest.getStallName(),
                notificationRequest.getStallSize(),
                notificationRequest.getBookingTime(),
                notificationRequest.getEventTime(),
                notificationRequest.getEventLink()
        );
        reservationEmailDetails.setUserName(notificationRequest.getUserName());
        return reservationEmailDetails;
    }

    @Transactional
    public void resendStallConfirmationEmail(Long reservationId, Long userId) {
        Optional<Notification> notificationOptional = getReservationNotification(reservationId, userId);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            if (notification.getStatus().equals(NotificationStatus.SENT)) {
                throw new IllegalArgumentException("The reservation with ID " + reservationId + " has already been confirmed and email sent.");
            }
            ReservationEmailDetails reservationEmailDetails = (ReservationEmailDetails) notification.getEmailDetails();
            try {
                ReservationNotificationRequest reservationNotificationRequest = new ReservationNotificationRequest(
                        notification.getUserId(),
                        notification.getRecipientEmail(),
                        reservationEmailDetails.getUserName(),
                        reservationId,
                        reservationEmailDetails.getFairName(),
                        reservationEmailDetails.getStallName(),
                        reservationEmailDetails.getStallSize(),
                        reservationEmailDetails.getBookingTime(),
                        reservationEmailDetails.getEventTime(),
                        reservationEmailDetails.getEventLink()
                );

                sendReservationConfirmationEmail(reservationNotificationRequest);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to resend confirmation email: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("No reservation found with ID " + reservationId + " for the provided email.");
        }
    }

    private static String getReservationConfirmationHTMLBody(ReservationEmailDetails reservationEmailDetails, String fairName, Long reservationId, String stallName, LocalDateTime bookingTime, LocalDateTime eventTime, URI eventLink, URI websiteUri) {
        return """
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8f9fa; color: #333; }
                        .container { max-width: 600px; background: #fff; margin: 30px auto; border-radius: 12px;
                                     box-shadow: 0 4px 15px rgba(0,0,0,0.1); overflow: hidden; }
                        .header { background-color: #5b3cc4; color: #fff; text-align: center; padding: 25px 20px; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .content { padding: 30px 40px; text-align: left; }
                        .details { margin-top: 20px; border-top: 1px solid #ddd; padding-top: 15px; line-height: 1.6; }
                        .qr-section { text-align: center; margin-top: 30px; }
                        #qrCode { width: 180px; height: 180px; border: 4px solid #eee; border-radius: 10px; }
                        .footer { background: #f1f1f1; text-align: center; padding: 15px; font-size: 13px; color: #666; }
                        .btn { display: inline-block; background-color: #5b3cc4; color: white; padding: 10px 20px;
                               border-radius: 5px; text-decoration: none; margin-top: 15px; }
                        .btn:hover { background-color: #4a2aa5; color: white; }
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
                                <img id="qrCode" src="cid:qrCode" alt="QR Code" />
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
                reservationEmailDetails.getUserName(),
                fairName,
                reservationId,
                stallName,
                bookingTime,
                eventTime,
                eventLink,
                websiteUri
        );
    }

    private String getAccountActivationEmailBody(AccountActivationEmailDetails accountActivationEmailDetails) {
        return """
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8f9fa; color: #333; }
                        .container { max-width: 600px; background: #fff; margin: 30px auto; border-radius: 12px;
                                     box-shadow: 0 4px 15px rgba(0,0,0,0.1); overflow: hidden; }
                        .header { background-color: #5b3cc4; color: #fff; text-align: center; padding: 25px 20px; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .content { padding: 30px 40px; text-align: left; }
                        .footer { background: #f1f1f1; text-align: center; padding: 15px; font-size: 13px; color: #666; }
                        .btn { display: inline-block; background-color: #5b3cc4; color: white; padding: 10px 20px;
                               border-radius: 5px; text-decoration: none; margin-top: 15px; }
                        .btn:hover { background-color: #4a2aa5; color: white; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to the Book Fair Platform, %s!</h1>
                        </div>
                        <div class="content">
                            <p>Your account has been successfully created.</p>
                            <p><b>Username:</b> %s</p>
                            <p>Click the button below to log in to your account:</p>
                            <a href="%s" class="btn">Log In</a>
                        </div>
                        <div class="footer">
                            <p>© 2025 Book Fair Committee |
                            <a href="%s" style="color:#5b3cc4;text-decoration:none;">Visit Website</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                accountActivationEmailDetails.getUserName(),
                accountActivationEmailDetails.getUserName(),
                accountActivationEmailDetails.getLoginLink(),
                this.websiteLink
        );
    }
}
