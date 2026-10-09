package com.arvind.payment.gateway;

import com.arvind.payment.exception.RazorpayVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

class RazorpayGatewayTest {

    private static final String KEY_ID = "rzp_test_key";
    private static final String KEY_SECRET = "test-secret";

    private MockRestServiceServer server;
    private RazorpayGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new RazorpayGateway(builder, KEY_ID, KEY_SECRET);
    }

    @Test
    void createsRazorpayOrderInMinorCurrencyUnits() {
        server.expect(requestTo("https://api.razorpay.com/v1/orders"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Basic cnpwX3Rlc3Rfa2V5OnRlc3Qtc2VjcmV0"))
                .andExpect(content().json("""
                        {
                          "amount": 1050,
                          "currency": "INR",
                          "receipt": "order-42",
                          "notes": {"order_id": "42"}
                        }
                        """))
                .andRespond(withSuccess(
                        "{\"id\":\"order_gateway_42\"}",
                        MediaType.APPLICATION_JSON
                ));

        assertEquals(
                "order_gateway_42",
                gateway.createOrder(42L, new BigDecimal("10.50"), "INR")
        );
        server.verify();
    }

    @Test
    void verifiesRazorpayCheckoutSignature() throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                KEY_SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        ));
        String signature = HexFormat.of().formatHex(
                mac.doFinal("order_gateway_42|pay_42".getBytes(StandardCharsets.UTF_8))
        );

        gateway.verifySignature("order_gateway_42", "pay_42", signature);
        assertThrows(
                RazorpayVerificationException.class,
                () -> gateway.verifySignature("order_gateway_42", "pay_42", "invalid")
        );
    }

    @Test
    void capturesOnlyPaymentMatchingExpectedOrderAmountAndCurrency() {
        server.expect(requestTo("https://api.razorpay.com/v1/payments/pay_42"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "order_id": "order_gateway_42",
                          "amount": 1050,
                          "currency": "INR",
                          "status": "authorized"
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.razorpay.com/v1/payments/pay_42/capture"))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {"amount": 1050, "currency": "INR"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "order_id": "order_gateway_42",
                          "amount": 1050,
                          "currency": "INR",
                          "status": "captured"
                        }
                        """, MediaType.APPLICATION_JSON));

        gateway.capturePayment(
                "pay_42",
                "order_gateway_42",
                new BigDecimal("10.50"),
                "INR"
        );
        server.verify();
    }
}
