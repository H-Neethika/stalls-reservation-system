
package com.payment.payment_service.service.impl;

import com.payment.payment_service.domain.PaymentOrderStatus;
import com.payment.payment_service.model.PaymentOrder;
import com.payment.payment_service.payload.request.CreatePaymentRequest;
import com.payment.payment_service.payload.response.PaymentIntentResponse;
import com.payment.payment_service.payload.response.PaymentOrderResponse;
import com.payment.payment_service.repository.PaymentOrderRepository;
import com.payment.payment_service.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.model.StripeObject;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.param.checkout.SessionCreateParams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;

    @Value("${STRIPE_SECRET_KEY:}")
    private String stripeSecretKey;

    @Value("${PAYMENT_CURRENCY:usd}")
    private String defaultCurrency;

    @Value("${STRIPE_WEBHOOK_SECRET:}")
    private String webhookSecret;

    @Value("${FRONTEND_BASE_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${STRIPE_SESSION_TTL_MINUTES:30}")
    private int checkoutTtlMinutes;

    @PostConstruct
    private void validateConfiguration() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY environment variable is required");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET environment variable is required");
        }
    }


    //  Create Checkout Session

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(CreatePaymentRequest request) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key not configured. Set STRIPE_SECRET_KEY environment variable.");
        }

        Stripe.apiKey = stripeSecretKey;

        String requestedCurrency = request.getCurrency();
        String resolvedCurrency = (requestedCurrency == null || requestedCurrency.isBlank())
                ? defaultCurrency
                : requestedCurrency;
        String currencyUpper = resolvedCurrency.toUpperCase();
        if (!("USD".equals(currencyUpper) || "LKR".equals(currencyUpper))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency. Allowed: USD, LKR");
        }
        String stripeCurrency = currencyUpper.toLowerCase(); // stripe expects lowercase

        BigDecimal originalAmount = request.getTotalAmount();
        if (originalAmount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalAmount is required");
        }
        if (originalAmount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalAmount must be at least 0.01");
        }

        BigDecimal normalizedAmount = originalAmount.setScale(2, RoundingMode.HALF_UP);
        Long stripeAmount = convertToStripeAmount(normalizedAmount, currencyUpper);

        log.info("Creating Stripe Checkout Session: reservationId={}, currency={}, originalAmount={}, convertedStripeAmount={}",
                request.getReservationId(), currencyUpper, normalizedAmount, stripeAmount);

        PaymentOrder order = PaymentOrder.builder()
                .reservationId(request.getReservationId())
                .amount(stripeAmount)
                .originalAmount(normalizedAmount)
                .convertedStripeAmount(stripeAmount)
                .currency(currencyUpper)
                .status(PaymentOrderStatus.PENDING)
                .build();
        order = paymentOrderRepository.save(order);

        try {
            // Product setup
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Reservation #" + request.getReservationId())
                            .build();

            // Price setup
            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(stripeCurrency)
                            .setUnitAmount(stripeAmount)
                            .setProductData(productData)
                            .build();

            // Line item
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            // URLs
            String successUrl = String.format("%s/payment/success?session_id={CHECKOUT_SESSION_ID}", frontendBaseUrl);
            String cancelUrl = String.format("%s/payment/cancel", frontendBaseUrl);

            // Session parameters
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(lineItem)
                    .putMetadata("reservationId", String.valueOf(request.getReservationId()))
                    .putMetadata("paymentOrderId", String.valueOf(order.getId()))
                    .build();

            Session session = Session.create(params);

            order.setSessionId(session.getId());
            if (session.getPaymentIntent() != null) {
                order.setStripePaymentIntentId(session.getPaymentIntent());
            }
            paymentOrderRepository.save(order);

            return PaymentIntentResponse.builder()
                    .orderId(order.getId())
                    .sessionId(session.getId())
                    .paymentIntentId(order.getStripePaymentIntentId())
                    .paymentUrl(session.getUrl())
                    .currency(currencyUpper)
                    .originalAmount(normalizedAmount)
                    .convertedStripeAmount(stripeAmount)
                    .status(order.getStatus())
                    .build();

        } catch (StripeException e) {
            order.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(order);
            throw new RuntimeException("Stripe error creating checkout session: " + e.getMessage(), e);
        }
    }


    //  Currency Conversion

    private Long convertToStripeAmount(BigDecimal amount, String currencyUpper) {
        int fractionDigits;
        switch (currencyUpper) {
            case "USD", "LKR" -> fractionDigits = 2;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency for conversion");
        }
        BigDecimal scaled = amount.scaleByPowerOfTen(fractionDigits);
        BigDecimal rounded = scaled.setScale(0, RoundingMode.HALF_UP);
        return rounded.longValueExact();
    }


    //  Get Latest Payment by Reservation ID

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderResponse getLatestOrderByReservationId(Long reservationId) {
        PaymentOrder order = paymentOrderRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
                .orElseThrow(() -> new RuntimeException("Payment order not found for reservationId=" + reservationId));
        return PaymentOrderResponse.builder()
                .orderId(order.getId())
                .reservationId(order.getReservationId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .paymentIntentId(order.getStripePaymentIntentId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


    //  Stripe Webhook Handler (fixed)

    @Override

    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Stripe-Signature header");
        }

        if (payload == null || payload.isBlank()) {
            log.error("❌ Webhook payload is null or empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty webhook payload");
        }

        log.debug("📦 Stripe webhook triggered. Payload length: {}", payload.length());

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (Exception e) {
            log.warn("❌ Stripe webhook signature verification failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook signature");
        }

        String type = event.getType();
        String eventId = event.getId();
        log.info("📦 Received Stripe event: type={}", type);

        // First, try to parse the raw JSON directly for better reliability
        try {
            JsonObject eventJson = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
            JsonObject dataObj = eventJson.getAsJsonObject("data");
            JsonObject objectData = dataObj.getAsJsonObject("object");

            handleEventByType(type, objectData);
            return;

        } catch (Exception e) {
            log.warn("⚠️ Direct JSON parsing failed for type={}. Error: {}", type, e.getMessage());
        }

        // Fallback to Stripe's deserialization
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> objOpt = deserializer.getObject();

        if (objOpt.isPresent()) {
            StripeObject obj = objOpt.get();
            handleStripeObject(type, obj);
        } else {
            log.error("❌ Both JSON parsing and Stripe deserialization failed for event type: {}", type);
        }
    }

    private void handleEventByType(String type, JsonObject objectData) {
        switch (type) {
            case "checkout.session.completed":
            case "checkout.session.expired":
                handleCheckoutSessionEvent(type, objectData);
                break;

            case "payment_intent.succeeded":
            case "payment_intent.canceled":
            case "payment_intent.payment_failed":
            case "payment_intent.processing":
            case "payment_intent.created":
                handlePaymentIntentEvent(type, objectData);
                break;

            case "charge.succeeded":
            case "charge.updated":
                handleChargeEvent(type, objectData);
                break;

            default:
                log.info("ℹ️ Unhandled event type: {}", type);
        }
    }

    private void handleCheckoutSessionEvent(String type, JsonObject sessionData) {
        JsonElement idElement = sessionData.get("id");
        if (idElement == null || idElement.isJsonNull()) {
            log.error("❌ No session ID found in checkout session event");
            return;
        }

        String sessionId = idElement.getAsString();
        log.info("🔍 Processing session event: {} for sessionId: {}", type, sessionId);

        paymentOrderRepository.findBySessionId(sessionId).ifPresentOrElse(order -> {
            PaymentOrderStatus oldStatus = order.getStatus();

            if ("checkout.session.completed".equals(type)) {
                order.setStatus(PaymentOrderStatus.SUCCEEDED);

                // Extract payment_intent if available
                JsonElement piElement = sessionData.get("payment_intent");
                if (piElement != null && !piElement.isJsonNull()) {
                    String paymentIntentId = piElement.getAsString();
                    order.setStripePaymentIntentId(paymentIntentId);
                    log.info("💳 Updated payment intent ID: {}", paymentIntentId);
                }

                log.info("✅ Payment completed for sessionId: {} (status: {} -> {})",
                        sessionId, oldStatus, order.getStatus());

            } else if ("checkout.session.expired".equals(type)) {
                order.setStatus(PaymentOrderStatus.CANCELED);
                log.info("⏰ Session expired for sessionId: {} (status: {} -> {})",
                        sessionId, oldStatus, order.getStatus());
            }

            paymentOrderRepository.save(order);

        }, () -> {
            log.warn("⚠️ No PaymentOrder found for sessionId: {}", sessionId);
        });
    }

    private void handlePaymentIntentEvent(String type, JsonObject paymentIntentData) {
        JsonElement idElement = paymentIntentData.get("id");
        if (idElement == null || idElement.isJsonNull()) {
            log.error("❌ No payment intent ID found in payment intent event");
            return;
        }

        String paymentIntentId = idElement.getAsString();
        log.info("🔍 Processing payment intent event: {} for paymentIntentId: {}", type, paymentIntentId);

        paymentOrderRepository.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(order -> {
            PaymentOrderStatus oldStatus = order.getStatus();
            PaymentOrderStatus newStatus = null;

            switch (type) {
                case "payment_intent.succeeded":
                    newStatus = PaymentOrderStatus.SUCCEEDED;
                    break;
                case "payment_intent.canceled":
                    newStatus = PaymentOrderStatus.CANCELED;
                    break;
                case "payment_intent.payment_failed":
                    newStatus = PaymentOrderStatus.FAILED;
                    break;
                case "payment_intent.processing":
                    newStatus = PaymentOrderStatus.REQUIRES_ACTION;
                    break;
                case "payment_intent.created":
                    // Don't change status for created events, just log
                    log.info("💳 Payment intent created: {}", paymentIntentId);
                    return;
                default:
                    log.warn("⚠️ Unhandled payment intent event type: {}", type);
                    return;
            }

            if (newStatus != null) {
                order.setStatus(newStatus);
                paymentOrderRepository.save(order);
                log.info("✅ Updated payment intent: {} (status: {} -> {})",
                        paymentIntentId, oldStatus, newStatus);
            }

        }, () -> {
            log.warn("⚠️ No PaymentOrder found for paymentIntentId: {}", paymentIntentId);
        });
    }

    private void handleChargeEvent(String type, JsonObject chargeData) {
        JsonElement piElement = chargeData.get("payment_intent");
        if (piElement == null || piElement.isJsonNull()) {
            log.info("ℹ️ Charge event {} has no payment_intent, skipping", type);
            return;
        }

        String paymentIntentId = piElement.getAsString();
        log.info("🔍 Processing charge event: {} for paymentIntentId: {}", type, paymentIntentId);

        // For charge events, we mainly just log since payment_intent events handle status updates
        paymentOrderRepository.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(order -> {
            log.info("💰 Charge {} for payment order: {} (reservationId: {})",
                    type, order.getId(), order.getReservationId());
        }, () -> {
            log.warn("⚠️ No PaymentOrder found for charge event paymentIntentId: {}", paymentIntentId);
        });
    }

    private void handleStripeObject(String type, StripeObject obj) {
        if (obj instanceof Session) {
            Session session = (Session) obj;
            String sessionId = session.getId();

            paymentOrderRepository.findBySessionId(sessionId).ifPresentOrElse(order -> {
                if ("checkout.session.completed".equals(type)) {
                    order.setStatus(PaymentOrderStatus.SUCCEEDED);
                    if (session.getPaymentIntent() != null) {
                        order.setStripePaymentIntentId(session.getPaymentIntent());
                    }
                    log.info("✅ [Object] Payment completed for sessionId: {}", sessionId);
                } else if ("checkout.session.expired".equals(type)) {
                    order.setStatus(PaymentOrderStatus.CANCELED);
                    log.info("⏰ [Object] Session expired for sessionId: {}", sessionId);
                }
                paymentOrderRepository.save(order);
            }, () -> log.warn("⚠️ [Object] No PaymentOrder found for sessionId: {}", sessionId));

        } else if (obj instanceof PaymentIntent) {
            PaymentIntent intent = (PaymentIntent) obj;
            String paymentIntentId = intent.getId();

            paymentOrderRepository.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(order -> {
                switch (type) {
                    case "payment_intent.succeeded":
                        order.setStatus(PaymentOrderStatus.SUCCEEDED);
                        break;
                    case "payment_intent.canceled":
                        order.setStatus(PaymentOrderStatus.CANCELED);
                        break;
                    case "payment_intent.payment_failed":
                        order.setStatus(PaymentOrderStatus.FAILED);
                        break;
                    case "payment_intent.processing":
                        order.setStatus(PaymentOrderStatus.REQUIRES_ACTION);
                        break;
                    default:
                        return;
                }
                paymentOrderRepository.save(order);
                log.info("✅ [Object] Updated payment intent: {} status for type: {}", paymentIntentId, type);
            }, () -> log.warn("⚠️ [Object] No PaymentOrder found for paymentIntentId: {}", paymentIntentId));
        }
    }
}
