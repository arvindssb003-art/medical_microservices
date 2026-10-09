package com.arvind.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {

        return requestTemplate -> {

            log.info("========== FEIGN INTERCEPTOR ==========");

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                log.warn("No servlet request context available");
                return;
            }

            HttpServletRequest request =
                    attributes.getRequest();

            String authorization =
                    request.getHeader("Authorization");

            boolean hasAuthorization =
                    authorization != null
                            && !authorization.isBlank();

            log.info(
                    "Authorization header present: {}, token starts with Bearer: {}",
                    hasAuthorization,
                    hasAuthorization && authorization.startsWith("Bearer ")
            );

            if (hasAuthorization) {

                requestTemplate.header(
                        "Authorization",
                        authorization
                );

                log.info(
                        "Authorization header forwarded to {}",
                        requestTemplate.url()
                );
            } else {
                log.warn(
                        "Authorization header NOT forwarded to {}",
                        requestTemplate.url()
                );
            }
        };
    }
}