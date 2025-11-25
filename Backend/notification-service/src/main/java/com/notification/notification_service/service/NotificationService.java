package com.notification.notification_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.notification_service.dto.AccountActivationNotificationRequest;
import com.notification.notification_service.dto.AccountActivationNotificationResponse;
import com.notification.notification_service.dto.ReservationNotificationRequest;
import com.notification.notification_service.dto.ReservationNotificationResponse;
import com.notification.notification_service.enums.EmailAttachmentType;
import com.notification.notification_service.enums.NotificationStatus;
import com.notification.notification_service.exception.*;
import com.notification.notification_service.mapper.NotificationMapper;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.model.email_details.AccountActivationEmailDetails;
import com.notification.notification_service.model.email_details.EmailDetails;
import com.notification.notification_service.model.email_details.ReservationEmailDetails;
import com.notification.notification_service.repository.NotificationRepository;
import com.notification.notification_service.service.event.EmailNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher publisher;
    private final QRCodeService qrCodeService;

    @Value("${OFFICIAL_WEBSITE_LINK}")
    private String websiteLink;

    @Value("${NOTIFICATION_QRCODE_SECRET:YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=}")
    private String base64Secret;

    @PostConstruct
    private void validateSecrets() {
        if (base64Secret == null || base64Secret.trim().isEmpty()) {
            throw new IllegalStateException("NOTIFICATION_QRCODE_SECRET is required but not configured");
        }

        // Validate that it's a proper base64 string and correct AES key length
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Secret);
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new IllegalStateException(String.format(
                    "NOTIFICATION_QRCODE_SECRET decoded to %d bytes, but AES requires 16, 24, or 32 bytes. " +
                    "Please use a properly sized base64 encoded key.", keyBytes.length));
            }
            log.info("QR code encryption secret validated successfully (AES-{} key)", keyBytes.length * 8);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("NOTIFICATION_QRCODE_SECRET must be a valid base64 encoded string", e);
        }
    }

    private Optional<Notification> getReservationNotification(Long reservationId, Long userId) {
        return notificationRepository.findAllByUserId(userId)
                .orElse(List.of())
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

    private Optional<Notification> getAccountCreationNotification(Long userId) {
        return notificationRepository.findAllByUserId(userId)
                .orElse(List.of())
                .stream()
                .filter(notification -> notification.getEmailDetails() instanceof AccountActivationEmailDetails)
                .findFirst();
    }

    public ReservationNotificationResponse getReservationNotificationStatus(Long reservationId, Long userId) {
        Notification notification = getReservationNotification(reservationId, userId).orElseThrow(
                () -> new NotificationNotFoundException("Reservation not found for ID: " + reservationId)
        );
        return NotificationMapper.toReservationNotificationResponse(notification);
    }

    public AccountActivationNotificationResponse getAccountActivationNotificationStatus(Long userId) {
        Notification notification = getAccountCreationNotification(userId).orElseThrow(
                () -> new NotificationNotFoundException("Account creation notification not found for ID: " + userId)
        );
        return NotificationMapper.toAccountActivationNotificationResponse(notification);
    }

    private byte[] createEncryptedQRCode(String qrcodeDetails, int width, int height) {
        try {
            // Validate inputs
            if (qrcodeDetails == null || qrcodeDetails.trim().isEmpty()) {
                throw new IllegalArgumentException("QR code details cannot be null or empty");
            }
            if (this.base64Secret == null || this.base64Secret.trim().isEmpty()) {
                throw new IllegalStateException("QR code encryption secret is not configured");
            }

            log.debug("Creating encrypted QR code with details length: {}, secret configured: true",
                    qrcodeDetails.length());

            String encrypted = AESEncryptor.encrypt(qrcodeDetails, this.base64Secret);
            return qrCodeService.generateQRCode(encrypted, width, height);
        } catch (Exception e) {
            log.error("Failed to create encrypted QR code. Details length: {}, Secret null: {}, Error: {}",
                    qrcodeDetails != null ? qrcodeDetails.length() : "null",
                    this.base64Secret == null,
                    e.getMessage(), e);
            throw new QRCodeGenerationException("Failed to create encrypted QR code: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getQRCodeDetails(String qrcodeSecret) {
        try {
            String decryptedJson = AESEncryptor.decrypt(qrcodeSecret, base64Secret);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(decryptedJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new QRCodeDecodingException("Failed to decode QR code details", e);
        }
    }


    private byte[] getQRCodeBytes(ReservationNotificationRequest notificationRequest) {
        Map<String, Object> qrcodeDetails = new HashMap<>();
        qrcodeDetails.put("reservationId", notificationRequest.getReservationId());
        qrcodeDetails.put("stallType", notificationRequest.getStallType());
        qrcodeDetails.put("stallName", notificationRequest.getStallName());
        qrcodeDetails.put("fairName", notificationRequest.getFairName());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(qrcodeDetails);

            return createEncryptedQRCode(jsonString, 500, 500);
        } catch (Exception e) {
            throw new QRCodeGenerationException("Failed to generate QR code", e);
        }
    }

    public byte[] getQRCodeBytes(Long reservationId, Long userId) {
        Notification notification = getReservationNotification(reservationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Reservation not found for ID: " + reservationId));
        ReservationNotificationRequest notificationRequest = NotificationMapper
                .toReservationNotificationRequest(notification);
        return getQRCodeBytes(notificationRequest);
    }

    @Transactional
    public void sendAccountActivationEmail(AccountActivationNotificationRequest notificationRequest) {

        String htmlBody = getAccountActivationEmailBody(notificationRequest);
        String email = notificationRequest.getEmail();
        Optional<Notification> optionalNotification = getAccountCreationNotification(notificationRequest.getUserId());

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (
                    notification.getStatus().equals(NotificationStatus.FAILED)
                            || notification.getStatus().equals(NotificationStatus.PENDING)
            ) {
                publisher.publishEvent(new EmailNotificationEvent(
                        notification.getId(),
                        email,
                        "Welcome to the Book Fair Platform!",
                        htmlBody,
                        true,
                        null,
                        null,
                        null
                ));

                return;
            }
            throw new NotificationAlreadySentException("An account creation email has already been sent to " + email);
        } else {
            Notification notification = NotificationMapper.toNotification(notificationRequest);
            notificationRepository.save(notification);

            publisher.publishEvent(new EmailNotificationEvent(
                    notification.getId(),
                    email,
                    "Welcome to the Book Fair Platform!",
                    htmlBody,
                    true,
                    null,
                    null,
                    null
            ));
        }
    }

    @Transactional
    public void resendAccountActivationEmail(Long userId) {
        Optional<Notification> notificationOptional = getAccountCreationNotification(userId);
        if (notificationOptional.isEmpty()) {
            throw new NotificationNotFoundException("No account creation notification found for user ID: " + userId);
        }

        Notification notification = notificationOptional.get();
        if (notification.getStatus().equals(NotificationStatus.SENT)) {
            throw new NotificationAlreadySentException("The account creation email has already been sent.");
        }

        AccountActivationNotificationRequest accountActivationNotificationRequest = NotificationMapper
                .toAccountActivationNotificationRequest(notification);
        sendAccountActivationEmail(accountActivationNotificationRequest);
    }

    @Transactional
    public void sendReservationConfirmationEmail(ReservationNotificationRequest notificationRequest) {
        log.info("Preparing reservation confirmation email: reservationId={}, userId={}, email={}",
                notificationRequest.getReservationId(), notificationRequest.getUserId(), notificationRequest.getEmail());
        if (notificationRequest.getEmail() == null || notificationRequest.getEmail().isBlank()) {
            log.error("Missing recipient email for reservationId={} userId={}. Skipping email send.",
                    notificationRequest.getReservationId(), notificationRequest.getUserId());
            return;
        }
        byte[] qrCodeBytes = getQRCodeBytes(notificationRequest);
        String htmlBody = getReservationConfirmationHTMLBody(
                notificationRequest,
                URI.create(this.websiteLink)
        );

        Optional<Notification> existingNotification = getReservationNotification(
                notificationRequest.getReservationId(),
                notificationRequest.getUserId()
        );
        if (existingNotification.isPresent()) {
            Notification existing = existingNotification.get();
            if (
                    existing.getStatus().equals(NotificationStatus.PENDING)
                            || existing.getStatus().equals(NotificationStatus.FAILED)
            ) {
                publisher.publishEvent(new EmailNotificationEvent(
                        existing.getId(),
                        notificationRequest.getEmail(),
                        notificationRequest.getFairName() + " - Reservation Confirmation",
                        htmlBody,
                        true,
                        "reservation_qr.png",
                        EmailAttachmentType.IMAGE,
                        qrCodeBytes
                ));
                existing.setStatus(NotificationStatus.PENDING);
                return;
            }
            throw new NotificationAlreadySentException(
                    "The reservation with ID " + notificationRequest.getReservationId() + " has already been confirmed."
            );
        }

        Notification notification = NotificationMapper.toNotification(notificationRequest);
        Notification savedNotification = notificationRepository.save(notification);

        publisher.publishEvent(new EmailNotificationEvent(
                savedNotification.getId(),
                notificationRequest.getEmail(),
                notificationRequest.getFairName() + " - Reservation Confirmation",
                htmlBody,
                true,
                "reservation_qr.png",
                EmailAttachmentType.IMAGE,
                qrCodeBytes
        ));
    }

    @Transactional
    public void resendStallConfirmationEmail(Long reservationId, Long userId) {
        Notification notification = getReservationNotification(reservationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "No reservation found with ID " + reservationId + " for this user."
                ));

        if (notification.getStatus().equals(NotificationStatus.SENT)) {
            throw new NotificationAlreadySentException(
                    "The reservation with ID " + reservationId + " has already been confirmed and email sent."
            );
        }

        ReservationNotificationRequest reservationNotificationRequest = NotificationMapper
                .toReservationNotificationRequest(notification);

        sendReservationConfirmationEmail(reservationNotificationRequest);
    }

    // --- HTML Template Methods ---
    private static String getReservationConfirmationHTMLBody(
            ReservationNotificationRequest notificationRequest,
            URI websiteUri
    ) {
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
                                <p><b>Stall Type:</b> %s</p>
                                <p><b>Hall:</b> %s</p>
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
                notificationRequest.getFairName(),
                notificationRequest.getUserName(),
                notificationRequest.getFairName(),
                notificationRequest.getReservationId(),
                notificationRequest.getStallName(),
                notificationRequest.getStallType(),
                notificationRequest.getHallName(),
                notificationRequest.getBookingTime(),
                notificationRequest.getEventTime(),
                notificationRequest.getEventLink(),
                websiteUri
        );
    }

    private String getAccountActivationEmailBody(AccountActivationNotificationRequest notificationRequest) {
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
                notificationRequest.getUserName(),
                notificationRequest.getUserName(),
                notificationRequest.getLoginLink(),
                this.websiteLink
        );
    }
}
