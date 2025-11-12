package com.notification.notification_service.controller;

import com.notification.notification_service.dto.AccountActivationNotificationRequest;
import com.notification.notification_service.dto.ReservationNotificationRequest;
import com.notification.notification_service.enums.NotificationType;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.service.NotificationService;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/reservation/details/{reservationId}")
    public ResponseEntity<Notification> getReservationNotificationDetails(@RequestParam Long userId, @PathVariable Long reservationId) {
        try {
            Optional<Notification> notification = notificationService.getReservationNotification(reservationId, userId);
            if (notification.isPresent()) {
                return ResponseEntity.ok(notification.get());
            } else {
                throw new IllegalArgumentException("There is no notification details for this reservation.");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @PostMapping(value = "/qr/generate/{reservationId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable String reservationId) {
        try {
            byte[] qrCode = notificationService.createQRCode(reservationId, 500, 500);
            return ResponseEntity.ok(qrCode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/account/send")
    public ResponseEntity<Void> sendAccountActivationNotification(@RequestBody AccountActivationNotificationRequest notificationRequest) {
        try {
            notificationService.sendAccountCreationEmail(notificationRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reservation/send")
    public ResponseEntity<Void> sendReservationNotification(@RequestBody ReservationNotificationRequest notificationRequest) {
        try {
            notificationService.sendReservationConfirmationEmail(notificationRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendNotification(
            @RequestParam Long userId,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) Long reservationId
    ) {
        try {
            switch (notificationType) {
                case STALL_RESERVATION -> {
                    notificationService.resendAccountCreationEmail(userId);
                }
                case ACCOUNT_ACTIVATION -> {
                    notificationService.resendStallConfirmationEmail(reservationId, userId);
                }
                case PASSWORD_RESET -> {}
                case EVENT_REMINDER -> {}
                default -> throw new IllegalArgumentException("Unsupported notification type: " + notificationType);
            }

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}