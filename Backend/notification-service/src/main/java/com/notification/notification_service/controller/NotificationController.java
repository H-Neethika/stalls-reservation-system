package com.notification.notification_service.controller;

import com.notification.notification_service.dto.BookingEvent;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.service.NotificationService;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{reservationId}")
    public ResponseEntity<Notification> getNotificationDetails(@PathVariable Long reservationId) {
        try {
            return ResponseEntity.ok(notificationService.getNotification(reservationId));
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

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody BookingEvent bookingEvent) {
        try {
            notificationService.processNotification(bookingEvent);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid booking bookingEvent: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending notification: " + e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendNotification(@RequestBody Long reservationId) {
        try {
            notificationService.resendNotification(reservationId);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid booking bookingEvent: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending notification: " + e.getMessage());
        }
    }

}