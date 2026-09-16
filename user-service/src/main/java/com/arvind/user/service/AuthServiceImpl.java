package com.arvind.user.service;

import com.arvind.user.dto.AuthResponse;
import com.arvind.user.dto.LoginRequest;
import com.arvind.user.dto.LogoutRequest;
import com.arvind.user.dto.RegisterRequest;
import com.arvind.user.dto.UserResponse;
import com.arvind.user.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
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
}