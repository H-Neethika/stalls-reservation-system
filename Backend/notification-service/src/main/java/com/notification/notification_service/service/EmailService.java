package com.notification.notification_service.service;

import com.notification.notification_service.service.event.EmailNotificationEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${NOTIFICATION_EMAIL}")
    private String notificationEmail;

    @Async("emailExecutor")
    public void sendEmail(
            UUID notificationId,
            String to,
            String subject,
            String body,
            boolean isHTMLBody,
            String attachmentFileName,
            EmailAttachmentType attachmentType,
            byte[] attachmentBytes
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(notificationEmail);
        helper.setTo(event.to());
        helper.setSubject(event.subject());
        helper.setText(event.body(), event.isHTMLBody());

        if (event.attachmentBytes() != null) {
            switch (event.attachmentType()) {
                case IMAGE -> {
                    ByteArrayResource src = new ByteArrayResource(event.attachmentBytes());
                    helper.addInline("qrCode", src, "image/png");
                    helper.addAttachment(event.attachmentFileName(), src, "image/png");
                }
                case PDF ->
                        helper.addAttachment(event.attachmentFileName(),
                                new ByteArrayResource(event.attachmentBytes()), "application/pdf");
                default -> throw new IllegalArgumentException(
                        "Unsupported attachment type: " + event.attachmentType());
            }
        }

        mailSender.send(message);
        logger.info("Email sent successfully to {}", event.to());
    }
}