package com.arvind.user.service;

import com.arvind.user.dto.AuthResponse;
import com.arvind.user.dto.LoginRequest;
import com.arvind.user.dto.LogoutRequest;
import com.arvind.user.dto.RegisterRequest;
import com.arvind.user.dto.UserResponse;
import com.arvind.user.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final RestClient restClient;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.id}")
    private String clientId;

    public AuthServiceImpl(Keycloak keycloak) {

        this.keycloak = keycloak;
        this.restClient = RestClient.create();
    }

    @Override
    public void register(RegisterRequest request) {

        UserRepresentation user = new UserRepresentation();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);

        Response response = keycloak
                .realm(realm)
                .users()
                .create(user);

        try {

            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException(
                        "Username or email already exists"
                );
            }

            if (response.getStatus() >= 400) {
                throw new RuntimeException(
                        "Failed to create user in Keycloak"
                );
            }

            String userId = CreatedResponseUtil.getCreatedId(response);

            CredentialRepresentation password =
                    new CredentialRepresentation();

            password.setType(CredentialRepresentation.PASSWORD);
            password.setValue(request.getPassword());
            password.setTemporary(false);

            keycloak
                    .realm(realm)
                    .users()
                    .get(userId)
                    .resetPassword(password);

            var patientRole = keycloak
                    .realm(realm)
                    .roles()
                    .get("PATIENT")
                    .toRepresentation();

            keycloak
                    .realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(List.of(patientRole));

        } finally {
            response.close();
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        Keycloak userKeycloak = null;

        try {
            userKeycloak = org.keycloak.admin.client.KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientId(clientId)
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .build();

            AccessTokenResponse tokenResponse =
                    userKeycloak.tokenManager().getAccessToken();

            return new AuthResponse(
                    tokenResponse.getToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn(),
                    tokenResponse.getRefreshExpiresIn(),
                    tokenResponse.getTokenType()
            );

        } finally {
            if (userKeycloak != null) {
                userKeycloak.close();
            }
        }
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        String tokenUrl = serverUrl
                + "/realms/"
                + realm
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);

        Map<String, Object> tokenResponse = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (tokenResponse == null
                || !(tokenResponse.get("access_token") instanceof String accessToken)
                || !(tokenResponse.get("refresh_token") instanceof String newRefreshToken)) {
            throw new IllegalStateException("Keycloak returned an incomplete token response");
        }

        return new AuthResponse(
                accessToken,
                newRefreshToken,
                numberValue(tokenResponse.get("expires_in")),
                numberValue(tokenResponse.get("refresh_expires_in")),
                (String) tokenResponse.get("token_type")
        );
    }

    private long numberValue(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    @Override
    public void logout(LogoutRequest request) {

        String logoutUrl = serverUrl
                + "/realms/"
                + realm
                + "/protocol/openid-connect/logout";

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        formData.add("client_id", clientId);
        formData.add("refresh_token", request.getRefreshToken());

        restClient.post()
                .uri(logoutUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity();
    }
    @Override
    public List<UserResponse> getAllUsers() {

        List<UserRepresentation> users = keycloak
                .realm(realm)
                .users()
                .list();

        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        Boolean.TRUE.equals(user.isEnabled())
                ))
                .toList();
    }
    @Override
    public UserResponse getUserById(String userId) {

        UserRepresentation user = keycloak
                .realm(realm)
                .users()
                .get(userId)
                .toRepresentation();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                Boolean.TRUE.equals(user.isEnabled())
        );
    }
}