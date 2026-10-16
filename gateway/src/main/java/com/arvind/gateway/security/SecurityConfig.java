package com.arvind.gateway.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/medicines/**", "/api/inventory/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/profile")
                        .hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/**")
                        .denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments")
                        .denyAll()
                        .requestMatchers(HttpMethod.POST, "/api/inventory/*/decrease")
                        .denyAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/inventory/*/decrease")
                        .denyAll()
                        .requestMatchers(
                                HttpMethod.POST, "/api/medicines/**",
                                "/api/inventory/**"
                        )
                        .denyAll()
                        .requestMatchers(
                                HttpMethod.PUT, "/api/medicines/**",
                                "/api/inventory/**"
                        )
                        .denyAll()
                        .requestMatchers(
                                HttpMethod.PATCH, "/api/medicines/**",
                                "/api/inventory/**"
                        )
                        .denyAll()
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/medicines/**",
                                "/api/inventory/**"
                        )
                        .denyAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/deliveries/**")
                        .denyAll()
                        .anyRequest().hasAnyRole("CUSTOMER", "ADMIN")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new KeycloakJwtAuthenticationConverter()
                        ))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174",
                "http://127.0.0.1:3000"
        ));
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}