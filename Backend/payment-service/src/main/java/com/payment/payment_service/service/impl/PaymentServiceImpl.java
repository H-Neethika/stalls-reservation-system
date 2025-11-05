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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${payment.currency:usd}")
    private String defaultCurrency;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.frontendBaseUrl:http://localhost:3000}")
    private String frontendBaseUrl;


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
        // Normalize to 2 decimal places
        BigDecimal normalizedAmount = originalAmount.setScale(2, RoundingMode.HALF_UP);
        Long stripeAmount = convertToStripeAmount(normalizedAmount, currencyUpper);

        log.info("Creating Stripe Checkout Session: reservationId={}, currency={}, originalAmount={}, convertedStripeAmount={}",
                request.getReservationId(), currencyUpper, normalizedAmount, stripeAmount);

        // Persist a new payment order in PENDING state
        PaymentOrder order = PaymentOrder.builder()
                .reservationId(request.getReservationId())
                .amount(stripeAmount) // keep legacy field as smallest unit
                .originalAmount(normalizedAmount)
                .convertedStripeAmount(stripeAmount)
                .currency(currencyUpper)
                .status(PaymentOrderStatus.PENDING)
                .build();
        order = paymentOrderRepository.save(order);

        try {
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Reservation #" + request.getReservationId())
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(stripeCurrency)
                            .setUnitAmount(stripeAmount) // expects smallest currency unit
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            String successUrl = String.format("%s/payment/success?session_id={CHECKOUT_SESSION_ID}", frontendBaseUrl);
            String cancelUrl = String.format("%s/payment/cancel", frontendBaseUrl);

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
            // PaymentIntent may be null until after completion; capture if available
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

    private Long convertToStripeAmount(BigDecimal amount, String currencyUpper) {
        // Currently both USD and LKR have 2 decimal places for Stripe
        int fractionDigits;
        switch (currencyUpper) {
            case "USD":
            case "LKR":
                fractionDigits = 2;
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency for conversion");
        }
        BigDecimal scaled = amount.scaleByPowerOfTen(fractionDigits);
        BigDecimal rounded = scaled.setScale(0, RoundingMode.HALF_UP);
        long value = rounded.longValueExact();
        log.debug("Converted {} {} to smallest unit: {}", amount, currencyUpper, value);
        return value;
    }

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

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret not configured. Set STRIPE_WEBHOOK_SECRET environment variable.");
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Stripe webhook signature: " + e.getMessage(), e);
        }

        String type = event.getType();
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (!deserializer.getObject().isPresent()) {
            // Cannot deserialize; ignore but don't fail the webhook
            return;
        }

        StripeObject obj = deserializer.getObject().get();

        if (obj instanceof Session) {
            Session session = (Session) obj;
            String sessionId = session.getId();
            paymentOrderRepository.findBySessionId(sessionId).ifPresent(order -> {
                if ("checkout.session.completed".equals(type)) {
                    order.setStatus(PaymentOrderStatus.SUCCEEDED);
                    // Backfill PaymentIntent id if available
                    if (session.getPaymentIntent() != null) {
                        order.setStripePaymentIntentId(session.getPaymentIntent());
                    }
                    paymentOrderRepository.save(order);
                }
            });
            return;
        }

        if (obj instanceof PaymentIntent) {
            PaymentIntent intent = (PaymentIntent) obj;
            String paymentIntentId = intent.getId();

            paymentOrderRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(order -> {
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
                        order.setStatus(PaymentOrderStatus.PENDING);
                        break;
                    default:
                        // no-op for other PI events
                        break;
                }
                paymentOrderRepository.save(order);
            });
        }
    }
}
