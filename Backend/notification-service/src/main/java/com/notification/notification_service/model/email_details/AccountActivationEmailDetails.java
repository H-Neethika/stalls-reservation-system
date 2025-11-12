package com.notification.notification_service.model.email_details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountActivationEmailDetails extends EmailDetails {
    private LocalDateTime createdTime;
    private String role;
    private URI loginLink;
}
