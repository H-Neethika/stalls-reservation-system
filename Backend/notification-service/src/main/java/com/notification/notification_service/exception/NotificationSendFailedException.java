package com.notification.notification_service.exception;

public class NotificationSendFailedException extends NotificationException {
    public NotificationSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationSendFailedException(String message) {
        super(message);
    }
}
