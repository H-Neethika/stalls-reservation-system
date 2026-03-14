# Stalls Reservation System 🚀

End-to-end stalls reservation platform with a Spring-based microservices backend and a React/Vite frontend. Vendors can browse exhibitions, reserve stalls, pay online, and receive QR-code confirmations, while organizers manage exhibitions, stalls, and payments from an internal portal.

## 🧭 Architecture Overview
- 🛡️ Cloud Gateway: routes client traffic (Spring Cloud Gateway).
- 🧭 Eureka Server: service discovery for all services.
- 👤 User Service: authentication, registration, JWT issuance.
- 🗂️ Booking Service: stall catalog, availability, reservations, and lifecycle.
- 🗓️ Exhibition Service: exhibition details and coordination with bookings/notifications.
- 💳 Payment Service: Stripe payments and callbacks.
- ✉️ Notification Service: emails, QR generation, and attachments.
- ⚡ Realtime Service: WebSocket updates for stall status.
- 🎨 Frontend: React + Vite UI for vendors and organizers.

## 📂 Project Structure
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
|-- .env.example         # Template env file (copy to .env)
`-- README.md
```

## ✨ Features (at a glance)
- ✅ Vendor flow: search exhibitions, view stall map, reserve and pay, receive QR confirmation.
- ✅ Organizer flow: manage exhibitions, halls/stalls, reservations, payment verification, realtime stall status.
- ✅ Security: JWT, OAuth2, gateway routing, service discovery.
- ✅ Tooling: Java 17 + Spring Boot ecosystem, React + Vite + TypeScript, Tailwind + ShadCN UI.

## 🧰 Prerequisites
- ☕ Java 17+, Maven
- 🟢 Node.js 18+ (or Bun) for the frontend
- 🐳 Docker + Docker Compose (for Kafka/ZooKeeper)
- 💳 Stripe CLI (for local webhook forwarding)

## 🔑 Environment Variables
Copy `.env.example` to `.env` and replace placeholders with your values. Full variable list and local defaults: [.env.example](./.env.example).

## 🐳 Start Kafka via Docker (optional but recommended)
From repo root:
```
docker compose -f Backend/docker-compose.kafka.yml up -d
```
This starts ZooKeeper and Kafka using ports and version from `.env`.

## 🏃 Running Locally
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

## 🔐 Default Organizer Account

The Stalls Reservation System includes a **predefined organizer account** that is required for managing exhibitions from the organizer portal.  
This account **cannot be created from the UI** and is automatically seeded into the `user_db` during project setup.

Use the following credentials to sign in as the organizer:

Email: organizer@gmail.com  
Password: Org@1234

Only **vendors** and **publishers** can register themselves through the frontend registration page.

## 💳 Stripe (local)
- Set `STRIPE_SECRET_KEY`, `STRIPE_API_PUBLISHABLE`, and `STRIPE_WEBHOOK_SECRET` in `.env`.
- Run the Stripe webhook forwarder (replace the key with yours):
```
stripe listen --forward-to http://localhost:9000/api/payment/webhook --api-key <your_stripe_secret_key>
```
- Payment service webhook endpoint: `POST /api/payment/webhook` (routed through the gateway on port `${CLOUD_GATEWAY_PORT}`).

## 🎨 Frontend Stack and Features
- React, Vite, TypeScript, Tailwind, ShadCN UI, Axios.
- Vendor portal: registration/login, stall selection with availability map, reservation confirmation, payments, QR/email handling, profile/history.
- Organizer portal: manage exhibitions, halls/stalls, view reservations, verify payments, realtime stall updates.
