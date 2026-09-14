# Config Server

This module runs the Spring Cloud Config Server on port 8888 using the native backend.

## Configuration boundaries

- `src/main/resources/application.yml` configures the Config Server itself. It selects the native profile, points the native backend at `classpath:/config-repo`, and exposes the server health/info endpoints.
- `src/main/resources/config-repo/application.yml` is common configuration served to client applications. It contains shared Eureka and Actuator settings.
- `src/main/resources/config-repo/api-gateway.yml` contains gateway-specific port, security issuer, and discovery-locator settings.
- `src/main/resources/config-repo/user-service.yml` contains user-service-specific port, security, and Keycloak settings.

Client applications keep their `spring.application.name` and mandatory `spring.config.import` in local `application.yml` files. The application name selects the matching file in `config-repo`; the common file is loaded for every client. The Config Server must be started before the client applications because imports are mandatory.

## Environment overrides

- `CONFIG_SERVER_URL` changes the Config Server URL used by clients.
- `EUREKA_SERVER_URL` changes the shared Eureka URL.
- `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_SERVER_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_ADMIN_USERNAME`, `KEYCLOAK_ADMIN_PASSWORD`, and `KEYCLOAK_CLIENT_ID` override Keycloak settings.
- `GATEWAY_PORT` and `USER_SERVICE_PORT` override service ports.

The defaults preserve the existing local-development behavior. Production deployments should provide credentials and environment-specific URLs through their deployment environment.
