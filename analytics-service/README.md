# Analytics Service

## Overview
The Analytics Service is a separate microservice that consumes events from the Account Service to provide real-time banking statistics and analytics using **CQRS (Command Query Responsibility Segregation)** and **Event Sourcing** patterns.

## Technology Stack
- **Java**: 17+
- **Spring Boot**: 3.3.1
- **Axon Framework**: 4.10.3
- **Database**: H2 (In-Memory)
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

## Architecture

### CQRS/Event Sourcing Architecture
This service implements the **CQRS** and **Event Sourcing** patterns using Axon Framework, matching the structure of the Account Service. It listens to events published by the Account Service and maintains aggregated statistics in a read model.

### Package Structure (CQRS Pattern)
```
com.ilyasgrid.analyticsservice
├── common_api
│   ├── events          # Event classes (must match Account Service)
│   │   ├── AccountCreatedEvent
│   │   ├── AccountCreditedEvent
│   │   └── AccountDebitedEvent
│   ├── enums
│   │   └── AccountStatus
│   └── queries         # Query definitions
│       └── GetBankStatsQuery
└── queries             # Query Side (Read Model)
    ├── entities        # JPA Entities (Read Model)
    │   └── BankStats
    ├── repositories    # Spring Data JPA Repositories
    │   └── BankStatsRepository
    ├── services        # Event Handlers & Query Handlers
    │   ├── AnalyticsEventHandler    # @EventHandler (Projection)
    │   └── AnalyticsQueryHandler    # @QueryHandler
    └── controllers     # REST API Controllers
        └── AnalyticsQueryController # Uses QueryGateway
```

## Key Components

### 1. Common API Layer

#### Events (com.ilyasgrid.common_api.events)
⚠️ **CRITICAL**: These event classes MUST be in the EXACT same package as Account Service for Axon event distribution:
- **AccountCreatedEvent**: Published when an account is created
- **AccountCreditedEvent**: Published when an account is credited
- **AccountDebitedEvent**: Published when an account is debited

#### Queries (com.ilyasgrid.analyticsservice.common_api.queries)
- **GetBankStatsQuery**: Query to retrieve current bank statistics

### 2. Query Side (Read Model)

#### BankStats Entity (queries.entities)
JPA entity storing aggregated banking statistics:
- `id`: Primary key (BANK_STATS_001)
- `totalBalance`: Sum of all account balances
- `accountCount`: Total number of accounts

#### BankStatsRepository (queries.repositories)
Spring Data JPA repository for BankStats entity.

#### AnalyticsEventHandler (queries.services)
**Event Projection** - Updates the read model when events occur:
- `@EventHandler on(AccountCreatedEvent)`: Increments account count and adds initial balance
- `@EventHandler on(AccountCreditedEvent)`: Adds credited amount to total balance
- `@EventHandler on(AccountDebitedEvent)`: Subtracts debited amount from total balance

#### AnalyticsQueryHandler (queries.services)
**Query Handler** - Handles queries for bank statistics:
- `@QueryHandler handle(GetBankStatsQuery)`: Retrieves current bank statistics from repository

#### AnalyticsQueryController (queries.controllers)
**REST API Controller** - Uses QueryGateway pattern (matching Account Service):
- `GET /queries/analytics/stats`: Get current bank statistics using QueryGateway

## Configuration

### Server Configuration
- **Port**: 8082
- **Application Name**: analytics-service

### Database Configuration
- **Type**: H2 In-Memory Database
- **URL**: jdbc:h2:mem:analytics
- **Console**: Enabled at `/h2-console`

### Axon Configuration
- **Event Serializer**: Jackson
- **Axon Server**: Disabled (using embedded event store)

## API Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8082/swagger-ui.html
```

### API Endpoints

#### Get Bank Statistics (Using QueryGateway)
```http
GET http://localhost:8082/queries/analytics/stats
```

**Response:**
```json
{
  "id": "BANK_STATS_001",
  "totalBalance": 15000.0,
  "accountCount": 5
}
```

**Implementation Details:**
- Uses Axon's `QueryGateway` to dispatch `GetBankStatsQuery`
- Returns `CompletableFuture<BankStats>` for async processing
- Query is handled by `AnalyticsQueryHandler` with `@QueryHandler` annotation

## Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Account Service running on port 8081

### Build and Run

#### Using Maven
```bash
cd analytics-service
mvn clean install
mvn spring-boot:run
```

#### Using Maven Wrapper
```bash
cd analytics-service
./mvnw clean install
./mvnw spring-boot:run
```

#### Using IDE (IntelliJ IDEA)
1. Open the project in IntelliJ IDEA
2. Right-click on `analytics-service/pom.xml`
3. Select "Maven" → "Reload Project"
4. Run `AnalyticsServiceApplication.java`

## Testing the Service

### 1. Start Both Services
- Account Service on port 8081
- Analytics Service on port 8082

### 2. Create an Account (Account Service)
```http
POST http://localhost:8081/api/commands/accounts/create
Content-Type: application/json

{
  "initialBalance": 1000.0,
  "currency": "USD"
}
```

### 3. Check Analytics (Using QueryGateway)
```http
GET http://localhost:8082/queries/analytics/stats
```

Expected response:
```json
{
  "id": "BANK_STATS_001",
  "totalBalance": 1000.0,
  "accountCount": 1
}
```

### 4. Credit an Account
```http
POST http://localhost:8081/api/commands/accounts/credit/{accountId}
Content-Type: application/json

{
  "amount": 500.0,
  "currency": "USD"
}
```

### 5. Verify Updated Analytics
```http
GET http://localhost:8082/queries/analytics/stats
```

Expected response:
```json
{
  "id": "BANK_STATS_001",
  "totalBalance": 1500.0,
  "accountCount": 1
}
```

## CQRS Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Account Service (Port 8081)                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  Command Side                                                               │
│  ┌──────────────┐      ┌──────────────────┐      ┌──────────────────┐     │
│  │   Command    │─────>│ AccountAggregate │─────>│  Event Published │     │
│  │  Controller  │      │  (Event Sourcing)│      │                  │     │
│  └──────────────┘      └──────────────────┘      └──────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ Events (via Axon)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Analytics Service (Port 8082)                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  Query Side (Read Model)                                                    │
│                                                                              │
│  ┌──────────────────────┐         ┌──────────────────┐                     │
│  │ AnalyticsEventHandler│────────>│   BankStats      │                     │
│  │   @EventHandler      │         │   (JPA Entity)   │                     │
│  │  - AccountCreated    │         │                  │                     │
│  │  - AccountCredited   │         │  - totalBalance  │                     │
│  │  - AccountDebited    │         │  - accountCount  │                     │
│  └──────────────────────┘         └──────────────────┘                     │
│                                            ▲                                │
│                                            │                                │
│  ┌──────────────────────┐         ┌──────────────────┐                     │
│  │AnalyticsQueryHandler │────────>│ BankStatsRepo    │                     │
│  │   @QueryHandler      │         │                  │                     │
│  └──────────────────────┘         └──────────────────┘                     │
│           ▲                                                                 │
│           │                                                                 │
│  ┌──────────────────────┐                                                  │
│  │AnalyticsQueryCtrl    │                                                  │
│  │  (QueryGateway)      │                                                  │
│  │  GET /queries/...    │                                                  │
│  └──────────────────────┘                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Event Flow Details

1. **Account Created**:
   - Account Service publishes `AccountCreatedEvent`
   - Analytics Service receives event via `@EventHandler`
   - Updates: `accountCount++`, `totalBalance += initialBalance`

2. **Account Credited**:
   - Account Service publishes `AccountCreditedEvent`
   - Analytics Service receives event via `@EventHandler`
   - Updates: `totalBalance += amount`

3. **Account Debited**:
   - Account Service publishes `AccountDebitedEvent`
   - Analytics Service receives event via `@EventHandler`
   - Updates: `totalBalance -= amount`

4. **Query Statistics**:
   - Client sends GET request to `/queries/analytics/stats`
   - Controller uses `QueryGateway` to dispatch `GetBankStatsQuery`
   - `AnalyticsQueryHandler` handles query with `@QueryHandler`
   - Returns current `BankStats` from database

## Critical Requirements

### 1. Event Package Matching
⚠️ **IMPORTANT**: Event classes in Analytics Service MUST be in the EXACT same package as Account Service:
- Package: `com.ilyasgrid.common_api.events`
- This is required for Axon Framework's distributed event handling
- Events are shared across microservices

### 2. Event Class Compatibility
All event classes must have:
- Identical package names
- Identical class names
- Identical field names and types
- Compatible serialization (Jackson)

### 3. CQRS Pattern Implementation
The Analytics Service follows the same CQRS structure as Account Service:
- **Query Side Only**: No command side (no aggregates)
- **Event Handlers**: Project events into read model (`@EventHandler`)
- **Query Handlers**: Handle queries from QueryGateway (`@QueryHandler`)
- **QueryGateway**: Used in controllers instead of direct repository access
- **Separation of Concerns**: Events update the model, queries read the model

## Troubleshooting

### Events Not Being Received
1. Verify both services are running
2. Check that event packages match exactly
3. Verify Axon configuration in both services
4. Check application logs for errors

### Database Issues
- Access H2 Console at: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:mem:analytics`
- Username: `sa`
- Password: (empty)

### Port Conflicts
If port 8082 is already in use, change it in `application.properties`:
```properties
server.port=8083
```

## Monitoring and Logging

The service provides detailed logging for:
- Event handling
- Statistics updates
- Database operations

Check console output for log messages like:
```
Handling AccountCreatedEvent for account: {accountId}
Updated stats - Total Balance: 1000.0, Account Count: 1
```

## CQRS Benefits in Analytics Service

### Why CQRS for Analytics?

1. **Separation of Concerns**:
   - Events update the read model (write path)
   - Queries retrieve data (read path)
   - No mixing of read/write logic

2. **Scalability**:
   - Read model can be optimized for queries
   - Can scale read and write sides independently
   - Multiple read models possible for different views

3. **Performance**:
   - Pre-aggregated data in BankStats
   - No complex joins or calculations at query time
   - Fast read operations

4. **Flexibility**:
   - Easy to add new projections
   - Can rebuild read model from events
   - Multiple query handlers for different use cases

## Comparison with Account Service

| Aspect | Account Service | Analytics Service |
|--------|----------------|-------------------|
| **Command Side** | ✅ Has Aggregates | ❌ No Aggregates |
| **Query Side** | ✅ Has Projections | ✅ Has Projections |
| **Event Handlers** | ✅ Updates Account entities | ✅ Updates BankStats entity |
| **Query Handlers** | ✅ Handles account queries | ✅ Handles stats queries |
| **QueryGateway** | ✅ Used in controllers | ✅ Used in controllers |
| **Event Publishing** | ✅ Publishes events | ❌ Only consumes events |

## Future Enhancements

Potential improvements:
- Add more detailed analytics (average balance, transaction counts, trends)
- Implement time-series data for historical analytics
- Add filtering and aggregation capabilities (by date, currency, etc.)
- Implement caching for frequently accessed statistics
- Add metrics and monitoring (Prometheus, Grafana)
- Implement event replay functionality for rebuilding projections
- Add multiple projections for different analytical views

## Dependencies

Key dependencies in `pom.xml`:
- `axon-spring-boot-starter` (4.10.3)
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-web`
- `springdoc-openapi-starter-webmvc-ui` (2.5.0)
- `h2` (runtime)
- `lombok`

## License
This project is part of a Microservices course assignment.
