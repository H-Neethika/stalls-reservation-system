package com.notification.notification_service.controller;

import com.notification.notification_service.dto.*;
import com.notification.notification_service.enums.NotificationType;
import com.notification.notification_service.service.NotificationService;
import com.notification.notification_service.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Fetch reservation notification details for a user.
     */
    @GetMapping("/reservation/details")
    public ResponseEntity<ReservationNotificationResponse> getReservationNotificationDetails(
            @RequestParam Long userId,
            @RequestParam Long reservationId
    ) {
        ReservationNotificationResponse response = notificationService.getReservationNotificationStatus(reservationId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch account activation notification details for a user
     */
    @GetMapping("/account/details")
    public ResponseEntity<AccountActivationNotificationResponse> getAccountActivationNotificationDetails(
            @RequestParam Long userId
    ) {
        AccountActivationNotificationResponse response = notificationService.getAccountActivationNotificationStatus(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate a QR code for a reservation.
     */
    @PostMapping(value = "/qr/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(
            @RequestParam Long reservationId,
            @RequestParam Long userId
    ) {
        byte[] qrCode = notificationService.getQRCodeBytes(reservationId, userId);
        return ResponseEntity.ok(qrCode);
    }

    /**
     * Get details from Scan QR code secret.
     */
    @GetMapping("/qr/scan")
    public ResponseEntity<Map<String, Object>> scanQRCode(@RequestBody ScannedQRCodeResponse scannedQRCodeResponse) {
        Map<String, Object> qrCodeDetails = notificationService.getQRCodeDetails(
                scannedQRCodeResponse.getEncryptedSecret()
        );
        return ResponseEntity.ok(qrCodeDetails);
    }

    /**
     * Send an account activation email.
     */
    @PostMapping("/account/send")
    public ResponseEntity<Void> sendAccountActivationNotification(
            @RequestBody AccountActivationNotificationRequest notificationRequest
    ) {
        notificationService.sendAccountActivationEmail(notificationRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * Send a reservation confirmation email.
     */
    @PostMapping("/reservation/send")
    public ResponseEntity<Void> sendReservationNotification(
            @RequestBody ReservationNotificationRequest notificationRequest
    ) {
        notificationService.sendReservationConfirmationEmail(notificationRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * Resend a notification based on type.
     */
    @PostMapping("/resend")
    public ResponseEntity<Void> resendNotification(@RequestBody ResendNotificationRequest request) {
        NotificationType type = request.getNotificationType();
        Long userId = request.getUserId();
        Long reservationId = request.getReservationId();

        switch (type) {
            case ACCOUNT_ACTIVATION ->
                    notificationService.resendAccountActivationEmail(userId);
            case STALL_RESERVATION -> {
                if (reservationId == null) {
                    throw new IllegalArgumentException("Reservation ID is required for STALL_RESERVATION notifications.");
                }
                notificationService.resendStallConfirmationEmail(reservationId, userId);
            }
            case PASSWORD_RESET, EVENT_REMINDER ->
                    throw new NotificationException("Resending for this type is not yet implemented: " + type);
            default ->
                    throw new NotificationException("Unsupported notification type: " + type);
        }

        return ResponseEntity.ok().build();
    }

}
