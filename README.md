# Stalls Reservation System - Microservices Architecture


# 🚀 System Overview

This system allows **publishers & vendors** to register, log in, view
available stalls, pay online, and receive reservation confirmation with
QR code.\
Organizers get an **employee-only portal** to manage exhibitions.

------------------------------------------------------------------------
# 📁 Project Structure

    stalls-reservation-system/
    │
    ├── Backend/
    │   ├── booking-service/          # Manages stalls, sizes, map, availability
    │   ├── cloud-gateway/            # API Gateway for routing (Spring Cloud Gateway)
    │   ├── eureka-server/            # Service discovery (Netflix Eureka)
    │   ├── exhibition-service/       # Handles exhibition details and call notification service
    │   ├── notification-service/     # Emails + QR code generation
    │   ├── payment-service/          # Payment integration for stall booking
    │   └── user-service/             # User management, auth, JWT
    │
    ├── frontend/
    │   ├── public/
    │   ├── src/
    │   │   ├── components/
    │   │   ├── contexts/
    │   │   ├── hooks/
    │   │   ├── lib/
    │   │   ├── pages/
    │   │   ├── services/            
    │   │   ├── types/
    │   │   └── utils/
    │   │
    │   ├── App.tsx
    │   ├── main.tsx
    │   ├── index.css
    │   └── index.html
    │
    └── README.md

    ------------------------------------------------------------------------

# 🧩 Microservices Breakdown

## 🔐 **User Service**

-   User registration & login
-   JWT authentication
-   Business validation (max 3 bookings)

## 📦 **Booking Service**

-   Stall listing (small, medium, large)
-   Stall map with availability
-   Update stall status
-   Reserve stalls
-   Manage booking state
-   Communicate with payment + exhibition service

## 🏢 **Exhibition Service**
-   Manage Exhibition details
-   Communicate with booking and notification service

## 💳 **Payment Service**

-   Secure payments
-   Payment confirmation callback
-   Finalizes reservations after success

## ✉️ **Notification Service**

-   Sends reservation email
-   Generates **QR code** (entry pass)
-   Sends downloadable attachments

## 🛣️ **Cloud Gateway**

-   Routes all frontend requests to microservices\
-   Central entry point

## 🔍 **Eureka Server**

-   Service registry & auto-discovery

------------------------------------------------------------------------

# 🧠 Frontend

### Built with:

-   React + Vite
-   TypeScript
-   ShadCN UI / Tailwind
-   Axios for API calls

### User Portal Features:

-   Registration & Login
-   Stall selection UI with map
-   Visual availability (green/gray)
-   Reservation confirmation popup
-   Payment redirection
-   Email + QR handling
-   Add literary genres
-   Profile & reservation history

### Employee Portal Features:

-   View all stalls
-   View all reservations
-   Payment verification

------------------------------------------------------------------------

# 💳 Payment Flow

1.  User selects stalls
2.  Booking service creates **pending reservation**
3.  Payment service processes payment
4.  On success:
    -   Booking confirmed
    -   QR generated
    -   Email sent
5.  Frontend redirects to success page

------------------------------------------------------------------------

# 📧 Email + QR Flow

-   Notification service generates a unique QR code linked to booking
    ID
-   Email includes:
    -   Stall details
    -   QR code (inline + attachment)
-   User enters exhibition using QR scan

------------------------------------------------------------------------

# 🛠️ Technologies Used

### **Backend**

-   Spring Boot
-   Spring Cloud
-   Spring Security + JWT
-   Spring Data JPA
-   Netflix Eureka
-   Cloud Gateway
-   PostgreSQL

### **Frontend**

-   React + Vite
-   TypeScript
-   Tailwind
-   Axios

------------------------------------------------------------------------

# ▶️ How to Run (Local)

### 1️⃣ Start Eureka Server

    cd Backend/eureka-server
    mvn spring-boot:run

### 2️⃣ Start Cloud Gateway

    cd Backend/cloud-gateway
    mvn spring-boot:run

### 3️⃣ Start All Services

Repeat:

    cd Backend/<service-name>
    mvn spring-boot:run

### 4️⃣ Start Frontend

    cd frontend
    npm install
    npm run dev

------------------------------------------------------------------------

# 📎 Repository

GitHub: **https://github.com/H-Neethika/stalls-reservation-system**


