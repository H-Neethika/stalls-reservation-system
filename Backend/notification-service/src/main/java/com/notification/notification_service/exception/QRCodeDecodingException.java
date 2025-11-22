package com.notification.notification_service.exception;

public class QRCodeDecodingException extends NotificationException {
    public QRCodeDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}