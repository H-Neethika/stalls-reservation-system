package com.notification.notification_service.exception;

public class NotificationAlreadySentException extends NotificationException {
    public NotificationAlreadySentException(String message) {
        super(message);
    }
}
