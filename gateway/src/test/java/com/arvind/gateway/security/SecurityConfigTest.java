package com.arvind.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SecurityConfigTest {

    private final CorsConfiguration corsConfiguration =
            new SecurityConfig()
                    .corsConfigurationSource()
                    .getCorsConfiguration(new MockHttpServletRequest(
                            "GET",
                            "/api/payments/order/1"
                    ));

    @Test
    void allowsViteDevelopmentPortsOnLoopbackHosts() {
        assertEquals(
                "http://localhost:5174",
                corsConfiguration.checkOrigin("http://localhost:5174")
        );
        assertEquals(
                "http://127.0.0.1:5174",
                corsConfiguration.checkOrigin("http://127.0.0.1:5174")
        );
    }

    @Test
    void rejectsOriginsOutsideLoopbackHosts() {
        assertNull(corsConfiguration.checkOrigin("http://192.168.1.10:5174"));
    }
}
