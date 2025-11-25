# Stalls Reservation System

End-to-end stalls reservation platform built with a Spring-based microservices backend and a React/Vite frontend. Vendors can browse exhibitions, reserve stalls, pay online, and receive QR-code confirmations, while organizers manage exhibitions and stalls via an internal portal.

## Architecture Overview
- **Cloud Gateway**: Routes all client traffic to backend services (Spring Cloud Gateway).
- **Eureka Server**: Service discovery for all microservices.
- **User Service**: Authentication, registration, JWT issuance.
- **Booking Service**: Stall catalog, availability, reservations, and booking lifecycle.
- **Exhibition Service**: Exhibition details, schedules, and coordination with bookings/notifications.
- **Payment Service**: Payment processing and success callbacks.
- **Notification Service**: Emails, QR generation, and attachments.
- **Realtime Service**: WebSocket updates for stall status.
- **Frontend**: React + Vite UI for vendors and organizers.

## Project Structure
```
stalls-reservation-system/
|-- Backend/
|   |-- booking-service/
|   |-- cloud-gateway/
|   |-- eureka-server/
|   |-- exhibition-service/
|   |-- notification-service/
|   |-- payment-service/
|   |-- realtime-service/
|   `-- user-service/
|-- frontend/
|   |-- public/
|   `-- src/
|       |-- components/
|       |-- contexts/
|       |-- hooks/
|       |-- lib/
|       |-- pages/
|       |-- services/
|       |-- types/
|       `-- utils/
|-- .env                 # Root env file shared by services and Vite
`-- README.md
```

## Environment Variables (.env)
Create a root `.env` file following this template (replace placeholders with your values). Comments show typical defaults used in local dev.

```
# Microservice ports
CLOUD_GATEWAY_PORT=9000
EUREKA_SERVER_PORT=9001
USER_SERVICE_PORT=9002
BOOKING_SERVICE_PORT=9003
EXHIBITION_SERVICE_PORT=9004
PAYMENT_SERVICE_PORT=9005
NOTIFICATION_SERVICE_PORT=9006
REALTIME_SERVICE_PORT=9010

# Frontend
FRONTEND_PORT=3030
FRONTEND_BASE_URL=http://localhost:${FRONTEND_PORT}
BACKEND_BASE_URL=http://localhost:${CLOUD_GATEWAY_PORT}
# REALTIME_SERVICE_BASE_URL=http://localhost:${REALTIME_SERVICE_PORT}
# VITE_REALTIME_WS_URL=${REALTIME_SERVICE_BASE_URL}/ws-stalls

# Eureka
EUREKA_DEFAULT_ZONE=http://localhost:${EUREKA_SERVER_PORT}/eureka

# JWT
APP_JWT_ISSUER=http://localhost:${USER_SERVICE_PORT}
APP_JWT_ACCESS_TOKEN_TTL=PT10H
APP_JWT_REFRESH_TOKEN_TTL=P7D

# OAuth2 client (user-service)
APP_OAUTH2_CLIENT_ID=user-service-client
APP_OAUTH2_CLIENT_SECRET=change-me
APP_OAUTH2_REDIRECT_URIS=http://localhost:${USER_SERVICE_PORT}/login/oauth2/code/user-service-client
APP_OAUTH2_POST_LOGOUT_URIS=http://localhost:${CLOUD_GATEWAY_PORT}/
APP_OAUTH2_FRONTEND_SUCCESS_URL=http://localhost:${FRONTEND_PORT}/oauth2/callback

# OAuth2 providers
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
GITHUB_REDIRECT_URI=http://localhost:${USER_SERVICE_PORT}/login/oauth2/code/github
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:${USER_SERVICE_PORT}/login/oauth2/code/google

# Databases
DB_USERNAME=postgres
DB_PASSWORD=your-db-password
BOOKING_DB_URL=jdbc:postgresql://localhost:5432/booking_db
USER_DB_URL=jdbc:postgresql://localhost:5432/user_db
EXHIBITION_DB_URL=jdbc:postgresql://localhost:5432/exhibition_db
PAYMENT_DB_URL=jdbc:postgresql://localhost:5432/payment_db
NOTIFICATION_DB_URL=jdbc:postgresql://localhost:5432/notification_db

# Stripe
STRIPE_SECRET_KEY=your-stripe-secret-key
STRIPE_API_PUBLISHABLE=your-stripe-publishable-key
STRIPE_WEBHOOK_SECRET=your-stripe-webhook-secret
PAYMENT_CURRENCY=usd

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Kafka
KAFKA_PORT=9092
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
BOOKING_PAYMENT_SUCCESS_GROUP=booking-payment-success
NOTIFICATION_RESERVATION_GROUP=notification-reservation
PAYMENT_SUCCESS_TOPIC=payment.success
RESERVATION_BOOKED_TOPIC=reservation.booked
STALL_STATUS_UPDATE_TOPIC=stall.status.update
STALL_STATUS_EVENTS_TOPIC=stall.status.events
DEFAULT_EVENT_LINK=https://bookfair.com/events/colombo-international-book-fair-2025
DEFAULT_FAIR_NAME=Stall Reservation System

# Notification service
NOTIFICATION_EMAIL=your-email@example.com
NOTIFICATION_EMAIL_PASSWORD=app-password-or-token
OFFICIAL_WEBSITE_LINK=https://bookfair.com/events/colombo-international-book-fair-2025
NOTIFICATION_QRCODE_SECRET=base64-secret

# Optional proxy (uncomment if needed)
# HTTP_PROXY_HOST=10.0.0.1
# HTTP_PROXY_PORT=3128
# HTTP_NON_PROXY_HOSTS=localhost|127.*|[::1]
```

## Running Locally
1) Start Eureka Server  
```
cd Backend/eureka-server
mvn spring-boot:run
```
2) Start Cloud Gateway  
```
cd Backend/cloud-gateway
mvn spring-boot:run
```
3) Start remaining services (new terminal per service)  
```
cd Backend/<service-name>
mvn spring-boot:run
```
4) Start the frontend  
```
cd frontend
npm install
npm run dev
```

## Frontend Stack and Features
- React, Vite, TypeScript, Tailwind, ShadCN UI, Axios.
- Vendor portal: registration/login, stall selection with availability map, reservation confirmation, payments, QR/email handling, profile/history.
- Organizer portal: manage exhibitions, halls/stalls, view reservations, verify payments, realtime stall updates.
