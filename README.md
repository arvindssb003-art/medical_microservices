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

## Contributors

- Surjeet0114
- Arvind Kumar