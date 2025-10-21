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
@RequestMapping("/api/v1")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notifications/{id}")
    public ResponseEntity<Notification> getNotificationDetails(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(notificationService.getNotificationDetails(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @PostMapping(value = "/qr/generate/{reservationId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable String reservationId) {
        try {
            byte[] qrCode = notificationService.getQRCode(reservationId);
            return ResponseEntity.ok(qrCode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/notifications/send")
    public ResponseEntity<String> sendNotification(@RequestBody BookingEvent event) {
        try {
            notificationService.processNotification(event);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid booking event: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending notification: " + e.getMessage());
        }
    }

}