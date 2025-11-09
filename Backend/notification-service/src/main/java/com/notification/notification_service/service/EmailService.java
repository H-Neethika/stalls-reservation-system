package com.notification.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendStallReservationConfirmation(String to, String fairName, String htmlBody, byte[] qrCodeBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@bookfair.com");
            helper.setTo(to);
            helper.setSubject("📚 Stall Reservation Confirmation - " + fairName);
            helper.setText(htmlBody, true);
            helper.addAttachment("Reservation_QR.png", new ByteArrayResource(qrCodeBytes), "image/png");

            mailSender.send(message);
            logger.info("✅ Stall reservation confirmation email sent to {}", to);

        } catch (MessagingException e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
