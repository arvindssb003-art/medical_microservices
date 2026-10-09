package com.arvind.payment.gateway;

import com.arvind.payment.exception.RazorpayVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component
public class RazorpayGateway {

    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayGateway(
            RestClient.Builder restClientBuilder,
            @Value("${razorpay.key-id:}") String keyId,
            @Value("${razorpay.key-secret:}") String keySecret) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.razorpay.com")
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public String getKeyId() {
        return keyId;
    }

    public String createOrder(Long localOrderId, BigDecimal amount, String currency) {
        requireCredentials();
        if (!"INR".equals(currency)) {
            throw new IllegalArgumentException("Only INR payments are supported");
        }
        long amountInMinorUnits = toMinorUnits(amount);

        Map<String, Object> response = restClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "amount", amountInMinorUnits,
                        "currency", currency,
                        "receipt", "order-" + localOrderId,
                        "notes", Map.of("order_id", localOrderId.toString())
                ))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        String razorpayOrderId = response == null ? null : stringValue(response.get("id"));
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            throw new IllegalStateException("Razorpay returned an order without an ID");
        }
        return razorpayOrderId;
    }

    public void verifySignature(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {
        requireCredentials();
        if (razorpayOrderId == null || razorpayOrderId.isBlank()
                || razorpayPaymentId == null || razorpayPaymentId.isBlank()
                || razorpaySignature == null || razorpaySignature.isBlank()) {
            throw new RazorpayVerificationException("Missing Razorpay payment verification data");
        }

        byte[] expectedSignature = generateSignature(
                razorpayOrderId + "|" + razorpayPaymentId
        );
        byte[] providedSignature;
        try {
            providedSignature = HexFormat.of().parseHex(razorpaySignature);
        } catch (IllegalArgumentException ex) {
            throw new RazorpayVerificationException("Invalid Razorpay payment signature");
        }

        if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
            throw new RazorpayVerificationException("Invalid Razorpay payment signature");
        }
    }

    public void capturePayment(
            String razorpayPaymentId,
            String expectedRazorpayOrderId,
            BigDecimal expectedAmount,
            String expectedCurrency) {
        requireCredentials();

        Map<String, Object> payment = restClient.get()
                .uri("/v1/payments/{paymentId}", razorpayPaymentId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (payment == null
                || !expectedRazorpayOrderId.equals(stringValue(payment.get("order_id")))
                || numberValue(payment.get("amount")) != toMinorUnits(expectedAmount)
                || !expectedCurrency.equals(stringValue(payment.get("currency")))) {
            throw new RazorpayVerificationException(
                    "Razorpay payment does not match the expected order"
            );
        }

        String status = stringValue(payment.get("status"));
        if ("captured".equals(status)) {
            return;
        }
        if (!"authorized".equals(status)) {
            throw new RazorpayVerificationException(
                    "Razorpay payment is not authorized or captured"
            );
        }

        Map<String, Object> capturedPayment = restClient.post()
                .uri("/v1/payments/{paymentId}/capture", razorpayPaymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "amount", toMinorUnits(expectedAmount),
                        "currency", expectedCurrency
                ))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (capturedPayment == null
                || !"captured".equals(stringValue(capturedPayment.get("status")))
                || !expectedRazorpayOrderId.equals(stringValue(capturedPayment.get("order_id")))
                || numberValue(capturedPayment.get("amount")) != toMinorUnits(expectedAmount)
                || !expectedCurrency.equals(stringValue(capturedPayment.get("currency")))) {
            throw new RazorpayVerificationException(
                    "Razorpay did not confirm payment capture"
            );
        }
    }

    static long toMinorUnits(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Payment amount must have no more than two decimal places",
                    ex
            );
        }
    }

    private byte[] generateSignature(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not verify Razorpay payment signature", ex);
        }
    }

    private void requireCredentials() {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new IllegalStateException(
                    "Razorpay credentials are not configured"
            );
        }
    }

    private static String stringValue(Object value) {
        return value instanceof String string ? string : null;
    }

    private static long numberValue(Object value) {
        return value instanceof Number number ? number.longValue() : -1;
    }
}
