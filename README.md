````markdown
# Payment Processor

A Spring Boot backend service for processing wallet payments with transaction safety, idempotency, and concurrency control.

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database
- JUnit 5
- Mockito
- MockMvc
- Maven

## Features

- Process payments between wallets
- Validate payment requests
- Prevent negative wallet balances
- Handle concurrent payment requests using database-level pessimistic locking
- Prevent duplicate payments using idempotency keys
- Store payment transactions
- Retrieve transactions by ID
- Return appropriate responses for successful and failed payments

## API

### Process Payment

`POST /payments`

Example request:

```json
{
  "senderId": "user-1",
  "receiverId": "user-2",
  "amount": 100.00,
  "idempotencyKey": "payment-123"
}
````

Successful response:

```json
{
  "transactionId": 1,
  "status": "SUCCESS",
  "message": "Payment successful"
}
```

### Get Payment

`GET /payments/{id}`

Returns the transaction associated with the given transaction ID.

## Concurrency Handling

Wallets are accessed using a database-level `PESSIMISTIC_WRITE` lock. This ensures that concurrent payments against the same wallet are processed one at a time.

For example, when ten concurrent requests attempt to debit ₹100 from a wallet containing ₹500, only five requests can succeed. The remaining requests fail with insufficient balance and the final balance remains ₹0.

## Idempotency

Each payment contains a unique idempotency key.

If the same payment request is received again, the existing transaction is returned instead of processing the payment again. The idempotency key also has a database-level unique constraint as an additional protection against duplicate transactions.

## Testing

The project uses an in-memory H2 database, so no external database setup is required.

On Windows, run:

```powershell
.\mvnw.cmd clean test
```

The test suite covers:

* Successful payment processing
* Concurrent identical idempotency requests
* Concurrent debit requests against a limited balance
* Insufficient balance
* Request validation
* Transaction retrieval
* Missing transaction handling

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/paymentprocessor/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── repository/
│   │       └── service/
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/example/paymentprocessor/
```

## Running the Application

Make sure Java 17 or later is installed.

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts locally and exposes the payment endpoints described above.

## Database

The application uses H2 for zero-configuration testing. The database is in-memory, so the test environment does not require MySQL, PostgreSQL, or any other external database.

## Design Decisions

See `DECISIONS.md` for the concurrency approach and implementation decisions made during development.

```
```
