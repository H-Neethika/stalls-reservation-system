package com.notification.notification_service.mapper;

import com.notification.notification_service.dto.AccountActivationNotificationRequest;
import com.notification.notification_service.dto.AccountActivationNotificationResponse;
import com.notification.notification_service.dto.ReservationNotificationRequest;
import com.notification.notification_service.dto.ReservationNotificationResponse;
import com.notification.notification_service.model.Notification;
import com.notification.notification_service.model.email_details.AccountActivationEmailDetails;
import com.notification.notification_service.model.email_details.EmailDetails;
import com.notification.notification_service.model.email_details.ReservationEmailDetails;

public class NotificationMapper {
    public static ReservationNotificationRequest toReservationNotificationRequest(
            Notification notification
    ) {
        ReservationNotificationRequest reservationNotificationRequest = new ReservationNotificationRequest();
        reservationNotificationRequest.setUserId(notification.getUserId());
        reservationNotificationRequest.setEmail(notification.getRecipientEmail());
        if(notification.getEmailDetails() instanceof ReservationEmailDetails emailDetails) {
            reservationNotificationRequest.setReservationId(emailDetails.getReservationId());
            reservationNotificationRequest.setUserName(emailDetails.getUserName());
            reservationNotificationRequest.setEventTime(emailDetails.getEventTime());
            reservationNotificationRequest.setEventLink(emailDetails.getEventLink());
            reservationNotificationRequest.setBookingTime(emailDetails.getBookingTime());
            reservationNotificationRequest.setFairName(emailDetails.getFairName());
            reservationNotificationRequest.setDisplayName(emailDetails.getDisplayName());
            reservationNotificationRequest.setStallName(emailDetails.getStallName());
            reservationNotificationRequest.setStallType(emailDetails.getStallType());
            reservationNotificationRequest.setHallName(emailDetails.getHallName());
        }

        return reservationNotificationRequest;
    }

    public static AccountActivationNotificationRequest toAccountActivationNotificationRequest(
            Notification notification
    ) {
        AccountActivationNotificationRequest activationNotificationRequest = new AccountActivationNotificationRequest();
        activationNotificationRequest.setUserId(notification.getUserId());
        activationNotificationRequest.setEmail(notification.getRecipientEmail());
        activationNotificationRequest.setNotificationType(notification.getNotificationType());
        if(notification.getEmailDetails() instanceof AccountActivationEmailDetails emailDetails) {
            activationNotificationRequest.setUserName(emailDetails.getUserName());
            activationNotificationRequest.setRole(emailDetails.getRole());
            activationNotificationRequest.setCreatedTime(emailDetails.getCreatedTime());
        }

        return activationNotificationRequest;
    }

    public static Notification toNotification(
            ReservationNotificationRequest reservationNotificationRequest
    ) {
        Notification notification = new Notification();
        notification.setUserId(reservationNotificationRequest.getUserId());
        notification.setRecipientEmail(reservationNotificationRequest.getEmail());
        notification.setNotificationType(reservationNotificationRequest.getNotificationType());

        EmailDetails emailDetails = new ReservationEmailDetails(
                reservationNotificationRequest.getReservationId(),
                reservationNotificationRequest.getFairName(),
                reservationNotificationRequest.getDisplayName(),
                reservationNotificationRequest.getStallName(),
                reservationNotificationRequest.getStallType(),
                reservationNotificationRequest.getHallName(),
                reservationNotificationRequest.getBookingTime(),
                reservationNotificationRequest.getEventTime(),
                reservationNotificationRequest.getEventLink()
        );
        emailDetails.setUserName(reservationNotificationRequest.getUserName());
        notification.setEmailDetails(emailDetails);

        return notification;
    }

    public static Notification toNotification(
            AccountActivationNotificationRequest activationNotificationRequest
    ) {
        Notification notification = new Notification();
        notification.setUserId(activationNotificationRequest.getUserId());
        notification.setRecipientEmail(activationNotificationRequest.getEmail());
        notification.setNotificationType(activationNotificationRequest.getNotificationType());

        EmailDetails emailDetails = new AccountActivationEmailDetails(
                activationNotificationRequest.getCreatedTime(),
                activationNotificationRequest.getRole(),
                activationNotificationRequest.getLoginLink()
        );
        emailDetails.setUserName(activationNotificationRequest.getUserName());
        notification.setEmailDetails(emailDetails);

        return notification;
    }

    public static EmailDetails toEmailDetails(Notification notification) {
        if (notification.getEmailDetails() instanceof ReservationEmailDetails emailDetails) {
            return new ReservationEmailDetails(
                    emailDetails.getReservationId(),
                    emailDetails.getFairName(),
                    emailDetails.getDisplayName(),
                    emailDetails.getStallName(),
                    emailDetails.getStallType(),
                    emailDetails.getHallName(),
                    emailDetails.getBookingTime(),
                    emailDetails.getEventTime(),
                    emailDetails.getEventLink()
            );
        }
        else if (notification.getEmailDetails() instanceof AccountActivationEmailDetails emailDetails) {
            return new AccountActivationEmailDetails(
                    emailDetails.getCreatedTime(),
                    emailDetails.getRole(),
                    emailDetails.getLoginLink()
            );
        } else {
            return null;
        }
    }

    public static ReservationEmailDetails toReservationEmailDetailsFromRequest(
            ReservationNotificationRequest notificationRequest
    ) {
        return new ReservationEmailDetails(
                notificationRequest.getReservationId(),
                notificationRequest.getFairName(),
                notificationRequest.getDisplayName(),
                notificationRequest.getStallName(),
                notificationRequest.getStallType(),
                notificationRequest.getHallName(),
                notificationRequest.getBookingTime(),
                notificationRequest.getEventTime(),
                notificationRequest.getEventLink()
        );
    }

    public static ReservationNotificationResponse toReservationNotificationResponse(Notification notification) {
        ReservationNotificationResponse reservationNotificationResponse = new ReservationNotificationResponse();
        reservationNotificationResponse.setUserId(notification.getUserId());
        reservationNotificationResponse.setNotificationType(notification.getNotificationType());
        reservationNotificationResponse.setStatus(notification.getStatus().toString());
        reservationNotificationResponse.setRecipientEmail(notification.getRecipientEmail());


        ReservationNotificationRequest notificationRequest = toReservationNotificationRequest(notification);

        reservationNotificationResponse.setBookingTime(notificationRequest.getBookingTime());
        reservationNotificationResponse.setEventTime(notificationRequest.getEventTime());
        reservationNotificationResponse.setReservationId(notificationRequest.getReservationId());
        reservationNotificationResponse.setFairName(notificationRequest.getFairName());
        reservationNotificationResponse.setDisplayName(notificationRequest.getDisplayName());
        reservationNotificationResponse.setStallName(notificationRequest.getStallName());
        reservationNotificationResponse.setStallType(notificationRequest.getStallType());
        reservationNotificationResponse.setHallName(notificationRequest.getHallName());
        reservationNotificationResponse.setUserName(notificationRequest.getUserName());
        reservationNotificationResponse.setEventLink(notificationRequest.getEventLink().toString());
        return reservationNotificationResponse;
    }

    public static AccountActivationNotificationResponse toAccountActivationNotificationResponse(Notification notification) {
        AccountActivationNotificationResponse accountActivationNotificationResponse = new AccountActivationNotificationResponse();
        accountActivationNotificationResponse.setUserId(notification.getUserId());
        accountActivationNotificationResponse.setStatus(notification.getStatus().toString());
        accountActivationNotificationResponse.setNotificationType(notification.getNotificationType());
        AccountActivationNotificationRequest notificationRequest = toAccountActivationNotificationRequest(notification);
        accountActivationNotificationResponse.setNotificationType(notificationRequest.getNotificationType());
        accountActivationNotificationResponse.setCreatedTime(notificationRequest.getCreatedTime());
        accountActivationNotificationResponse.setRecipientEmail(notification.getRecipientEmail());
        return accountActivationNotificationResponse;
    }
}
