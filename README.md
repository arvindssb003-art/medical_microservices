# Medical Microservices

A medical e-commerce microservices platform built with Spring Boot,
Spring Cloud, PostgreSQL, Keycloak and Python.

## Services

| Service | Responsibility |
|---|---|
| Auth Service | Authentication |
| User Service | User management |
| Medicine Service | Medicine management |
| Inventory Service | Stock management |
| Cart Service | Shopping cart |
| Order Service | Order management |
| Payment Service | Payments |
| Prescription Service | Prescriptions |
| Delivery Service | Delivery |
| Notification Service | Notifications |
| Gateway | API Gateway |
| Service Registry | Service Discovery |
| Config Server | Centralized Configuration |
| Chatbot Service | AI chatbot |

## Tech Stack

### Backend
- Java
- Spring Boot
- Spring Cloud
- Spring Data JPA
- PostgreSQL

### Infrastructure
- Eureka
- Spring Cloud Gateway
- Spring Cloud Config
- Keycloak

### AI
- Python
- FastAPI
- LLM
- RAG

## Project Structure

...

## Getting Started

...

## Running the Services

...

## Razorpay Checkout

Set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` before starting the services.
The payment service creates a Razorpay order after it receives an order payment
request. The client can poll `GET /api/payments/order/{orderId}` until the
payment response contains `razorpayOrderId` and `razorpayKeyId`, then use those
values with Razorpay Checkout. Post the Checkout result to
`POST /api/payments/order/{orderId}/verify` with `razorpayPaymentId` and
`razorpaySignature`. The service verifies the signature, confirms/captures the
payment with Razorpay, and only then emits the payment-completed event.

## Kafka Failure Handling

Order and payment services persist outgoing Kafka events in their own
`outbox_events` database table in the same transaction as the related order or
payment change. A scheduled publisher sends pending rows and retries publishing
failures with backoff. Delivery is at-least-once: a process crash after Kafka
accepts an event but before the database marks it published can result in a
duplicate, so consumers must be idempotent.

Consumer processing failures are retried with exponential backoff before the
record is published to the corresponding `.DLT` topic (for example,
`payment.requested.DLT`). DLT records require operator review and manual replay
after the underlying issue is resolved; the services do not currently replay
DLT records automatically.

## Role-Based Access

The gateway and secured services map Keycloak realm roles from
`realm_access.roles` to Spring Security roles. Self-registration assigns the
`CUSTOMER` realm role, so create that realm role before enabling registration.
Existing accounts need the `CUSTOMER` role assigned in Keycloak as well.
Create and assign the `ADMIN` realm role only to trusted operator accounts.
Customer APIs require `CUSTOMER`, while medicine/inventory reads and the listed
authentication POST endpoints remain public. Medicine and inventory writes
through the gateway require `ADMIN`; inventory stock-decrease routes remain
reserved for the order flow. User administration, payment listing, and
delivery status updates remain denied pending their own admin policies. Login
and token-refresh responses include a `roles` array from the validated
Keycloak access token; `GET /api/users/profile` also returns the current user's
realm roles so a client can display them.

The medicine admin API supports single create/update and initial inventory
creation using the existing `/api/medicines` and `/api/inventory` endpoints.
Bulk import uses `POST /api/medicines/import/preview` with multipart field
`file`, followed by `POST /api/medicines/import/{importId}/commit`. Imports
accept CSV, XLS, and XLSX files with the headers in the UI's downloadable CSV
template. Preview reports row-level validation errors; commit rejects any
preview containing invalid rows and creates the medicine and inventory records
for valid imports. Image upload is not implemented yet and is optional in the
UI.

Customer cart operations use `/api/cart/me`; order and payment ownership is
derived from the access token subject, not a caller-supplied user ID. Delivery
reads are scoped to the authenticated customer, and creating a delivery first
checks that the supplied order belongs to that customer.

## Production Compose Profile

The production deployment layer is opt-in and leaves the local
`docker-compose.yml` unchanged. Copy `production.env.example` to
`.env.production`, replace every placeholder, and provide the values through
your deployment platform's secret store rather than committing real
credentials. The production overlay requires Docker Compose v2.24 or newer.

Validate the merged configuration before starting it:

```sh
docker compose -p medical-production \
  --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.production.yml \
  config --quiet
```

Build and start the production stack with:

```sh
docker compose -p medical-production \
  --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.production.yml \
  up -d --build
```

The production project name and container names keep its containers and named
volumes separate from the local stack. The overlay binds only the gateway to
the host (loopback by default) and removes published ports for PostgreSQL,
Kafka, the config server, service registry, and user service. Put a TLS
reverse proxy or load balancer in front of the gateway. Set
`GATEWAY_BIND_ADDRESS` only when the chosen ingress needs a different host
interface.

Keycloak remains external to this first backend deployment profile. Its
public HTTPS issuer must be identical to the `iss` claim in issued tokens and
reachable from the gateway and secured services. The profile currently runs
PostgreSQL and Kafka as single-node Compose services and uses one application
database role across schemas; it is a deployment starting point, not a
high-availability production architecture. Prefer managed PostgreSQL/Kafka,
per-service database credentials, Docker/platform secrets, backups, monitoring,
and a documented recovery process before handling production traffic.

## Contributors

- Surjeet0114
- Arvind Kumar