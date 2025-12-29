# Analytics Service - CQRS Architecture

## Complete System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ACCOUNT SERVICE (Port 8081)                             │
│                     CQRS + Event Sourcing                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                        COMMAND SIDE                                 │    │
│  │                                                                      │    │
│  │  POST /api/commands/accounts/create                                 │    │
│  │  POST /api/commands/accounts/credit/{id}                            │    │
│  │  POST /api/commands/accounts/debit/{id}                             │    │
│  │                           │                                          │    │
│  │                           ▼                                          │    │
│  │              ┌─────────────────────────┐                            │    │
│  │              │  AccountAggregate       │                            │    │
│  │              │  @Aggregate             │                            │    │
│  │              │  - Event Sourcing       │                            │    │
│  │              └─────────────────────────┘                            │    │
│  │                           │                                          │    │
│  │                           │ Publishes Events                         │    │
│  │                           ▼                                          │    │
│  └────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                         QUERY SIDE                                  │    │
│  │                                                                      │    │
│  │  ┌──────────────────────┐         ┌──────────────────┐            │    │
│  │  │AccountServiceHandler │────────>│   Account        │            │    │
│  │  │  @EventHandler       │         │   (JPA Entity)   │            │    │
│  │  └──────────────────────┘         └──────────────────┘            │    │
│  │                                            ▲                        │    │
│  │  ┌──────────────────────┐         ┌──────────────────┐            │    │
│  │  │ AccountQueryHandler  │────────>│ AccountRepository│            │    │
│  │  │  @QueryHandler       │         │                  │            │    │
│  │  └──────────────────────┘         └──────────────────┘            │    │
│  │           ▲                                                         │    │
│  │           │                                                         │    │
│  │  ┌──────────────────────┐                                          │    │
│  │  │AccountQueryController│                                          │    │
│  │  │  (QueryGateway)      │                                          │    │
│  │  │  GET /queries/...    │                                          │    │
│  │  └──────────────────────┘                                          │    │
│  └────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    │   AXON EVENT BUS (In-Memory)  │
                    │               │               │
                    │  AccountCreatedEvent          │
                    │  AccountCreditedEvent         │
                    │  AccountDebitedEvent          │
                    │               │               │
                    └───────────────┼───────────────┘
                                    │
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                   ANALYTICS SERVICE (Port 8082)                             │
│                   CQRS (Query Side Only)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                    COMMON API (Shared)                              │    │
│  │                                                                      │    │
│  │  ┌──────────────────────────────────────────────────────────┐      │    │
│  │  │  com.ilyasgrid.common_api.events                         │      │    │
│  │  │  - AccountCreatedEvent  (MUST match Account Service)     │      │    │
│  │  │  - AccountCreditedEvent (MUST match Account Service)     │      │    │
│  │  │  - AccountDebitedEvent  (MUST match Account Service)     │      │    │
│  │  └──────────────────────────────────────────────────────────┘      │    │
│  │                                                                      │    │
│  │  ┌──────────────────────────────────────────────────────────┐      │    │
│  │  │  com.ilyasgrid.analyticsservice.common_api.queries       │      │    │
│  │  │  - GetBankStatsQuery                                     │      │    │
│  │  └──────────────────────────────────────────────────────────┘      │    │
│  └────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                         QUERY SIDE                                  │    │
│  │                                                                      │    │
│  │  ┌──────────────────────┐         ┌──────────────────┐            │    │
│  │  │AnalyticsEventHandler │────────>│   BankStats      │            │    │
│  │  │  @EventHandler       │         │   (JPA Entity)   │            │    │
│  │  │                      │         │                  │            │    │
│  │  │  on(AccountCreated)  │         │  - id            │            │    │
│  │  │  on(AccountCredited) │         │  - totalBalance  │            │    │
│  │  │  on(AccountDebited)  │         │  - accountCount  │            │    │
│  │  └──────────────────────┘         └──────────────────┘            │    │
│  │                                            ▲                        │    │
│  │                                            │                        │    │
│  │  ┌──────────────────────┐         ┌──────────────────┐            │    │
│  │  │AnalyticsQueryHandler │────────>│BankStatsRepository│           │    │
│  │  │  @QueryHandler       │         │                  │            │    │
│  │  │                      │         │                  │            │    │
│  │  │  handle(GetBankStats)│         │                  │            │    │
│  │  └──────────────────────┘         └──────────────────┘            │    │
│  │           ▲                                                         │    │
│  │           │                                                         │    │
│  │           │ QueryGateway.query()                                   │    │
│  │           │                                                         │    │
│  │  ┌──────────────────────┐                                          │    │
│  │  │AnalyticsQueryCtrl    │                                          │    │
│  │  │  (QueryGateway)      │                                          │    │
│  │  │                      │                                          │    │
│  │  │  GET /queries/       │                                          │    │
│  │  │      analytics/stats │                                          │    │
│  │  └──────────────────────┘                                          │    │
│  └────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ❌ NO COMMAND SIDE (No Aggregates, No Commands)                           │
│  ✅ QUERY SIDE ONLY (Event Handlers + Query Handlers)                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. Account Creation Flow

```
Client
  │
  │ POST /api/commands/accounts/create
  │ { initialBalance: 1000, currency: "USD" }
  │
  ▼
AccountCommandController (Account Service)
  │
  │ commandGateway.send(CreateAccountCommand)
  │
  ▼
AccountAggregate
  │
  │ AggregateLifecycle.apply(AccountCreatedEvent)
  │
  ▼
Event Store + Event Bus
  │
  ├──────────────────────────────────────┐
  │                                      │
  ▼                                      ▼
AccountServiceHandler              AnalyticsEventHandler
(Account Service)                  (Analytics Service)
  │                                      │
  │ @EventHandler                        │ @EventHandler
  │ on(AccountCreatedEvent)              │ on(AccountCreatedEvent)
  │                                      │
  ▼                                      ▼
Account Entity                     BankStats Entity
- Save new account                 - accountCount++
                                   - totalBalance += 1000
```

### 2. Query Flow

```
Client
  │
  │ GET /queries/analytics/stats
  │
  ▼
AnalyticsQueryController
  │
  │ queryGateway.query(GetBankStatsQuery)
  │
  ▼
AnalyticsQueryHandler
  │
  │ @QueryHandler
  │ handle(GetBankStatsQuery)
  │
  ▼
BankStatsRepository
  │
  │ findById("BANK_STATS_001")
  │
  ▼
BankStats Entity
  │
  │ { id: "BANK_STATS_001",
  │   totalBalance: 1000.0,
  │   accountCount: 1 }
  │
  ▼
Client (JSON Response)
```

## Package Comparison

### Account Service
```
com.ilyasgrid.eventsoucingwithaxon
├── command
│   ├── aggregates
│   │   └── AccountAggregate.java
│   └── controllers
│       └── AccountCommandController.java
├── common
│   ├── commands
│   ├── dtos
│   ├── enums
│   ├── events                    ← Shared with Analytics
│   └── queries
└── query
    ├── controllers
    │   └── AccountQueryController.java
    ├── entity
    │   └── Account.java
    ├── repositories
    │   └── AccountRepository.java
    └── service
        ├── AccountQueryHandler.java
        └── AccountServiceHandler.java
```

### Analytics Service
```
com.ilyasgrid
├── common_api                     ← Shared package
│   ├── enums
│   └── events                     ← MUST match Account Service
│       ├── AccountCreatedEvent.java
│       ├── AccountCreditedEvent.java
│       └── AccountDebitedEvent.java
└── analyticsservice
    ├── common_api
    │   └── queries
    │       └── GetBankStatsQuery.java
    └── queries                    ← Query Side Only
        ├── controllers
        │   └── AnalyticsQueryController.java
        ├── entities
        │   └── BankStats.java
        ├── repositories
        │   └── BankStatsRepository.java
        └── services
            ├── AnalyticsEventHandler.java
            └── AnalyticsQueryHandler.java
```

## Key Differences

| Aspect | Account Service | Analytics Service |
|--------|----------------|-------------------|
| **Purpose** | Manage accounts | Aggregate statistics |
| **Command Side** | ✅ Yes (Aggregates) | ❌ No |
| **Query Side** | ✅ Yes (Projections) | ✅ Yes (Projections) |
| **Event Publishing** | ✅ Publishes events | ❌ Only consumes |
| **Event Handling** | ✅ Updates Account | ✅ Updates BankStats |
| **Aggregates** | AccountAggregate | None |
| **Entities** | Account | BankStats |
| **Commands** | Create, Credit, Debit | None |
| **Queries** | GetAccount, GetAll | GetBankStats |

## CQRS Principles Applied

### ✅ Separation of Concerns
- **Account Service**: Handles commands and maintains account state
- **Analytics Service**: Consumes events and provides analytics

### ✅ Event-Driven Communication
- Services communicate via events
- Loose coupling between services
- Asynchronous processing

### ✅ Read Model Optimization
- BankStats is pre-aggregated
- No complex calculations at query time
- Fast read operations

### ✅ Scalability
- Services can scale independently
- Read and write sides separated
- Multiple read models possible

### ✅ Consistency
- Eventual consistency via events
- Transactional event handling
- Reliable event delivery

## Technology Stack

### Both Services
- **Java 17**
- **Spring Boot 3.3.1**
- **Axon Framework 4.10.3**
- **H2 Database** (In-Memory)
- **Lombok**
- **SpringDoc OpenAPI**

### Axon Components Used

#### Account Service
- `@Aggregate`
- `@CommandHandler`
- `@EventSourcingHandler`
- `@EventHandler`
- `@QueryHandler`
- `CommandGateway`
- `QueryGateway`

#### Analytics Service
- `@EventHandler`
- `@QueryHandler`
- `QueryGateway`

## Running the System

### 1. Start Account Service
```bash
cd account-service
./mvnw spring-boot:run
```
- Port: 8081
- Swagger: http://localhost:8081/swagger-ui.html

### 2. Start Analytics Service
```bash
cd analytics-service
./mvnw spring-boot:run
```
- Port: 8082
- Swagger: http://localhost:8082/swagger-ui.html

### 3. Test the Flow
1. Create account → Check analytics (count: 1)
2. Credit account → Check analytics (balance increased)
3. Debit account → Check analytics (balance decreased)

## Monitoring

### Logs to Watch

**Account Service:**
```
AccountCreatedEvent received: updating Read Model
```

**Analytics Service:**
```
Handling AccountCreatedEvent for account: {id}
Updated stats - Total Balance: 1000.0, Account Count: 1
```

### Database Inspection

**Account Service H2:**
- URL: http://localhost:8081/h2-console
- JDBC: jdbc:h2:mem:axon
- Table: ACCOUNT

**Analytics Service H2:**
- URL: http://localhost:8082/h2-console
- JDBC: jdbc:h2:mem:analytics
- Table: BANK_STATS

## Success Criteria

✅ Analytics Service follows CQRS pattern matching Account Service
✅ Events are in the correct shared package
✅ QueryGateway is used in controllers
✅ Event handlers update the read model
✅ Query handlers respond to queries
✅ No command side in Analytics Service
✅ Proper separation of concerns
✅ Swagger documentation available
✅ All components properly annotated
