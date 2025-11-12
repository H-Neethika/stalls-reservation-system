package com.notification.notification_service.service;

import com.notification.notification_service.enums.EmailAttachmentType;
import com.notification.notification_service.enums.NotificationStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationStatusService notificationStatusService;

    @Value("${NOTIFICATION_EMAIL}")
    private String notificationEmail;

    @Async
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
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHTMLBody);

            if (attachmentBytes != null) {
                switch (attachmentType) {
                    case IMAGE -> {
                        ByteArrayResource inputStreamSource = new ByteArrayResource(attachmentBytes);
                        helper.addInline("qrCode", inputStreamSource, "image/png");
                        helper.addAttachment(attachmentFileName, inputStreamSource, "image/png");
                    }
                    case PDF -> helper.addAttachment(attachmentFileName, new ByteArrayResource(attachmentBytes), "application/pdf");
                    default -> throw new IllegalArgumentException("Unsupported attachment type: " + attachmentType);
                }
            }

            mailSender.send(message);
            logger.info("✅ Email sent successfully to {}", to);

            // Update status → SENT
            notificationStatusService.updateStatus(notificationId, NotificationStatus.SENT, null);

        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());

            // Update status → FAILED
            notificationStatusService.updateStatus(notificationId, NotificationStatus.FAILED, e.getMessage());
        }
    }
}
