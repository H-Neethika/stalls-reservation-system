package com.notification.notification_service.model.email_details;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "notificationType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReservationEmailDetails.class, name = "STALL_RESERVATION"),
        @JsonSubTypes.Type(value = AccountActivationEmailDetails.class, name = "ACCOUNT_ACTIVATION")
})
public abstract class EmailDetails {
    private String userName;
}
