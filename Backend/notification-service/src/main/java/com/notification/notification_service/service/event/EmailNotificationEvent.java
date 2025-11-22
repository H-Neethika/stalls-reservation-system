package com.notification.notification_service.service.event;

import com.notification.notification_service.enums.EmailAttachmentType;

import java.util.UUID;

public record EmailNotificationEvent(
        UUID notificationId,
        String to,
        String subject,
        String body,
        boolean isHTMLBody,
        String attachmentFileName,
        EmailAttachmentType attachmentType,
        byte[] attachmentBytes
) { }