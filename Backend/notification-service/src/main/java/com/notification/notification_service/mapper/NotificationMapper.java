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

    // ----------- RESERVATION REQUEST FROM ENTITY -------------
    public static ReservationNotificationRequest toReservationNotificationRequest(Notification notification) {
        ReservationNotificationRequest request = new ReservationNotificationRequest();
        request.setUserId(notification.getUserId());
        request.setEmail(notification.getRecipientEmail());

        if (notification.getEmailDetails() instanceof ReservationEmailDetails details) {
            request.setReservationId(details.getReservationId());
            request.setUserName(details.getUserName());
            request.setEventTime(details.getEventTime());
            request.setEventLink(details.getEventLink());
            request.setBookingTime(details.getBookingTime());
            request.setFairName(details.getFairName());
            request.setDisplayName(details.getDisplayName());
            request.setStalls(details.getStalls());  // <-- updated
        }

        return request;
    }

    // ----------- ACCOUNT ACTIVATION REQUEST FROM ENTITY -------------
    public static AccountActivationNotificationRequest toAccountActivationNotificationRequest(Notification notification) {
        AccountActivationNotificationRequest req = new AccountActivationNotificationRequest();
        req.setUserId(notification.getUserId());
        req.setEmail(notification.getRecipientEmail());
        req.setNotificationType(notification.getNotificationType());

        if (notification.getEmailDetails() instanceof AccountActivationEmailDetails details) {
            req.setUserName(details.getUserName());
            req.setRole(details.getRole());
            req.setCreatedTime(details.getCreatedTime());
            req.setLoginLink(details.getLoginLink());
        }

        return req;
    }

    // ----------- CREATE NOTIFICATION ENTITY (RESERVATION) -------------
    public static Notification toNotification(ReservationNotificationRequest req) {
        Notification notification = new Notification();
        notification.setUserId(req.getUserId());
        notification.setRecipientEmail(req.getEmail());
        notification.setNotificationType(req.getNotificationType());

        EmailDetails details = new ReservationEmailDetails(
                req.getReservationId(),
                req.getFairName(),
                req.getDisplayName(),
                req.getStalls(),               // <-- list of stalls
                req.getBookingTime(),
                req.getEventTime(),
                req.getEventLink()
        );

        details.setUserName(req.getUserName());
        notification.setEmailDetails(details);
        return notification;
    }

    // ----------- CREATE NOTIFICATION ENTITY (ACCOUNT ACTIVATION) -------------
    public static Notification toNotification(AccountActivationNotificationRequest req) {
        Notification notification = new Notification();
        notification.setUserId(req.getUserId());
        notification.setRecipientEmail(req.getEmail());
        notification.setNotificationType(req.getNotificationType());

        EmailDetails details = new AccountActivationEmailDetails(
                req.getCreatedTime(),
                req.getRole(),
                req.getLoginLink()
        );

        details.setUserName(req.getUserName());
        notification.setEmailDetails(details);
        return notification;
    }

    // ----------- COPY EMAIL DETAILS -------------
    public static EmailDetails toEmailDetails(Notification notification) {
        if (notification.getEmailDetails() instanceof ReservationEmailDetails details) {
            return new ReservationEmailDetails(
                    details.getReservationId(),
                    details.getFairName(),
                    details.getDisplayName(),
                    details.getStalls(),      // <-- list support
                    details.getBookingTime(),
                    details.getEventTime(),
                    details.getEventLink()
            );
        }

        if (notification.getEmailDetails() instanceof AccountActivationEmailDetails details) {
            return new AccountActivationEmailDetails(
                    details.getCreatedTime(),
                    details.getRole(),
                    details.getLoginLink()
            );
        }

        return null;
    }

    // ----------- COPY RESERVATION DETAILS FROM REQUEST -------------
    public static ReservationEmailDetails toReservationEmailDetailsFromRequest(ReservationNotificationRequest req) {
        return new ReservationEmailDetails(
                req.getReservationId(),
                req.getFairName(),
                req.getDisplayName(),
                req.getStalls(),
                req.getBookingTime(),
                req.getEventTime(),
                req.getEventLink()
        );
    }

    // ----------- BUILD RESERVATION RESPONSE -------------
    public static ReservationNotificationResponse toReservationNotificationResponse(Notification notification) {
        ReservationNotificationRequest req = toReservationNotificationRequest(notification);

        ReservationNotificationResponse res = new ReservationNotificationResponse();
        res.setUserId(notification.getUserId());
        res.setNotificationType(notification.getNotificationType());
        res.setStatus(notification.getStatus().toString());
        res.setRecipientEmail(notification.getRecipientEmail());

        res.setBookingTime(req.getBookingTime());
        res.setEventTime(req.getEventTime());
        res.setReservationId(req.getReservationId());
        res.setFairName(req.getFairName());
        res.setDisplayName(req.getDisplayName());
        res.setStalls(req.getStalls());               // <-- UPDATED
        res.setUserName(req.getUserName());
        res.setEventLink(req.getEventLink().toString());

        return res;
    }

    // ----------- BUILD ACCOUNT ACTIVATION RESPONSE -------------
    public static AccountActivationNotificationResponse toAccountActivationNotificationResponse(Notification notification) {
        AccountActivationNotificationRequest req = toAccountActivationNotificationRequest(notification);

        AccountActivationNotificationResponse res = new AccountActivationNotificationResponse();
        res.setUserId(notification.getUserId());
        res.setStatus(notification.getStatus().toString());
        res.setNotificationType(req.getNotificationType());
        res.setCreatedTime(req.getCreatedTime());
        res.setRecipientEmail(notification.getRecipientEmail());

        return res;
    }
}
