# KnickKnack Backend API

KnickKnack is a modern, reliable Trust & Rating-based peer-to-peer equipment sharing platform for campus communities. This Spring Boot backend provides secure user authentication, item checkout/return via unique QR nonces, and a dedicated trust score model to incentivize good borrower behavior.

## Tech Stack
- **Framework**: Java 21, Spring Boot 3.5.9
- **Database**: MongoDB (Spring Data MongoDB)
- **Security**: Spring Security + JWT Authentication
- **Documentation**: Swagger UI / OpenAPI 3.0
- **Build**: Maven

## Getting Started

### Prerequisites
- JDK 21+
- Maven
- MongoDB (running locally on port 27017, or update `MONGO_URI`)

### Environment Variables
Configure the following in your environment for deployment (they fall back to defaults in `application.yaml` for local development):
- `MONGO_URI`: The connection string for your MongoDB instance.
- `JWT_SECRET`: A secure 256-bit base64 encoded string for JWT signing.
- `PORT`: The server port (defaults to 8080).

### Running the Application
```bash
# Compile and package
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

## API Documentation
The API is fully documented using Swagger UI. Once the application is running, you can access the interactive documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Docs**: `http://localhost:8080/v3/api-docs`

## Core Workflows (Examples)

### 1. Registration & Login
Register a new user to start interacting:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john.doe@example.com", "password": "Password123!", "phone": "1234567890", "campusId": "C123"}'
```

Log in to receive your JWT token:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com", "password": "Password123!"}'
# Note the token returned for subsequent authenticated requests
```

### 2. Available Items
View items that are currently available to borrow:
```bash
curl -X GET http://localhost:8080/items \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### 3. Reserving an Item
Reserve an item using its Item ID to lock it for checkout:
```bash
curl -X POST http://localhost:8080/reservations/reserve-item \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"itemId": "<ITEM_ID>"}'
# This endpoint generates a specific `checkoutQrNonce`
```

### 4. Checkout Item
The owner scans the `checkoutQrNonce` (typically embedded in a QR code) to officially check the item out to the borrower:
```bash
curl -X POST http://localhost:8080/reservations/checkout \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"qrCode": "<CHECKOUT_QR_NONCE>"}'
# Generates the `returnQrNonce` and sets the `expectedReturnAt` timestamp.
```

### 5. Return Item
When the item is returned, the owner verifies receipt by scanning the newly generated `returnQrNonce`:
```bash
curl -X POST http://localhost:8080/reservations/return \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"qrCode": "<RETURN_QR_NONCE>"}'
# The return logic determines if the item was late, allocates trust score penalties (-2 to -30) or rewards (+5), and spawns an empty Rating document.
```

### 6. Submitting a Rating
After completion, rate the transaction (1-5 scale) to modify the peer's Trust Score:
```bash
curl -X POST http://localhost:8080/ratings \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "<RESERVATION_ID>",
    "score": 5,
    "comment": "Great transaction!",
    "ratingType": "BORROWER" 
  }'
# `ratingType` specifies if you are reviewing as the OWNER rating the BORROWER, or vice versa.
```
