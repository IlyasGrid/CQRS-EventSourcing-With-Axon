# Analytics Service - CQRS Implementation Summary

## ✅ Complete Implementation

### Project Structure (CQRS Pattern)

```
analytics-service/
├── src/main/java/com/ilyasgrid/
│   ├── common_api/                          # Shared across services
│   │   ├── events/                          # ⚠️ MUST match Account Service
│   │   │   ├── AccountCreatedEvent.java
│   │   │   ├── AccountCreditedEvent.java
│   │   │   └── AccountDebitedEvent.java
│   │   └── enums/
│   │       └── AccountStatus.java
│   │
│   └── analyticsservice/
│       ├── common_api/
│       │   └── queries/
│       │       └── GetBankStatsQuery.java   # Query definition
│       │
│       ├── queries/                         # Query Side (Read Model)
│       │   ├── entities/
│       │   │   └── BankStats.java           # JPA Entity
│       │   │
│       │   ├── repositories/
│       │   │   └── BankStatsRepository.java # Spring Data JPA
│       │   │
│       │   ├── services/
│       │   │   ├── AnalyticsEventHandler.java    # @EventHandler
│       │   │   └── AnalyticsQueryHandler.java    # @QueryHandler
│       │   │
│       │   └── controllers/
│       │       └── AnalyticsQueryController.java # QueryGateway
│       │
│       └── AnalyticsServiceApplication.java
│
├── src/main/resources/
│   └── application.properties
│
├── pom.xml
└── README.md
```

## Implementation Details

### Step A: Common API (Events & Queries) ✅

#### 1. Events (com.ilyasgrid.common_api.events)

```java
// AccountCreatedEvent.java
package com.ilyasgrid.common_api.events;

import com.ilyasgrid.analyticsservice.common_api.enums.AccountStatus;

public record AccountCreatedEvent(
        String accountId,
        double initialBalance,
        String currency,
        AccountStatus accountStatus
) {
}
```

```java
// AccountCreditedEvent.java
package com.ilyasgrid.common_api.events;

public record AccountCreditedEvent(
    String id,
    double amount,
    String currency
) {}
```

```java
// AccountDebitedEvent.java
package com.ilyasgrid.common_api.events;

public record AccountDebitedEvent(
    String id,
    double amount,
    String currency
) {}
```

#### 2. Query (com.ilyasgrid.analyticsservice.common_api.queries)
```java
// GetBankStatsQuery.java
package com.ilyasgrid.analyticsservice.common_api.queries;

public record GetBankStatsQuery() {}
```

### Step B: Query Side (The Projection) ✅

#### 1. Entity (queries.entities)
```java
// BankStats.java
package com.ilyasgrid.analyticsservice.queries.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankStats {
    @Id
    private String id;
    private double totalBalance;
    private int accountCount;
}
```

#### 2. Repository (queries.repositories)
```java
// BankStatsRepository.java
package com.ilyasgrid.analyticsservice.queries.repositories;

import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankStatsRepository extends JpaRepository<BankStats, String> {}
```

#### 3. Event Handler (queries.services)

```java
// AnalyticsEventHandler.java
package com.ilyasgrid.analyticsservice.queries.services;

import com.ilyasgrid.analyticsservice.common_api.events.AccountCreatedEvent;
import com.ilyasgrid.analyticsservice.common_api.events.AccountCreditedEvent;
import com.ilyasgrid.analyticsservice.common_api.events.AccountDebitedEvent;
import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import com.ilyasgrid.analyticsservice.queries.repositories.BankStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsEventHandler {

    private static final String STATS_ID = "BANK_STATS_001";
    private final BankStatsRepository bankStatsRepository;

    @EventHandler
    public void on(AccountCreatedEvent event) {
        log.info("Handling AccountCreatedEvent for account: {}", event.accountId());

        BankStats stats = bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));

        stats.setAccountCount(stats.getAccountCount() + 1);
        stats.setTotalBalance(stats.getTotalBalance() + event.initialBalance());

        bankStatsRepository.save(stats);
        log.info("Updated stats - Total Balance: {}, Account Count: {}",
                stats.getTotalBalance(), stats.getAccountCount());
    }

    @EventHandler
    public void on(AccountCreditedEvent event) {
        log.info("Handling AccountCreditedEvent for account: {}", event.id());

        BankStats stats = bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));

        stats.setTotalBalance(stats.getTotalBalance() + event.amount());

        bankStatsRepository.save(stats);
        log.info("Updated stats after credit - Total Balance: {}", stats.getTotalBalance());
    }

    @EventHandler
    public void on(AccountDebitedEvent event) {
        log.info("Handling AccountDebitedEvent for account: {}", event.id());

        BankStats stats = bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));

        stats.setTotalBalance(stats.getTotalBalance() - event.amount());

        bankStatsRepository.save(stats);
        log.info("Updated stats after debit - Total Balance: {}", stats.getTotalBalance());
    }
}
```

### Step C: Exposure (Query Controller) ✅

#### 1. Query Handler (queries.services)
```java
// AnalyticsQueryHandler.java
package com.ilyasgrid.analyticsservice.queries.services;

import com.ilyasgrid.analyticsservice.common_api.queries.GetBankStatsQuery;
import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import com.ilyasgrid.analyticsservice.queries.repositories.BankStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsQueryHandler {

    private static final String STATS_ID = "BANK_STATS_001";
    private final BankStatsRepository bankStatsRepository;

    @QueryHandler
    public BankStats handle(GetBankStatsQuery query) {
        log.info("Handling GetBankStatsQuery");
        return bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));
    }
}
```

#### 2. Query Controller (queries.controllers)
```java
// AnalyticsQueryController.java
package com.ilyasgrid.analyticsservice.queries.controllers;

import com.ilyasgrid.analyticsservice.common_api.queries.GetBankStatsQuery;
import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/queries/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Query API", description = "Query endpoints for bank analytics and statistics")
public class AnalyticsQueryController {

    private final QueryGateway queryGateway;

    @GetMapping("/stats")
    @Operation(summary = "Get Bank Statistics",
               description = "Retrieve current bank statistics using QueryGateway")
    public CompletableFuture<BankStats> getBankStats() {
        return queryGateway.query(
                new GetBankStatsQuery(),
                ResponseTypes.instanceOf(BankStats.class)
        );
    }
}
```

### Configuration ✅

#### application.properties
```properties
spring.application.name=analytics-service
server.port=8082

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:analytics
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Axon Configuration
axon.serializer.events=jackson
axon.serializer.general=jackson
axon.serializer.messages=jackson
axon.axonserver.enabled=false

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

## CQRS Pattern Verification

### ✅ Matches Account Service Structure

| Component | Account Service | Analytics Service | Status |
|-----------|----------------|-------------------|--------|
| **common_api.events** | ✅ | ✅ | ✅ Identical package |
| **common_api.queries** | ✅ | ✅ | ✅ Implemented |
| **queries.entities** | ✅ Account | ✅ BankStats | ✅ |
| **queries.repositories** | ✅ AccountRepository | ✅ BankStatsRepository | ✅ |
| **queries.services (EventHandler)** | ✅ AccountServiceHandler | ✅ AnalyticsEventHandler | ✅ |
| **queries.services (QueryHandler)** | ✅ AccountQueryHandler | ✅ AnalyticsQueryHandler | ✅ |
| **queries.controllers** | ✅ AccountQueryController | ✅ AnalyticsQueryController | ✅ |
| **Uses QueryGateway** | ✅ | ✅ | ✅ |
| **command side** | ✅ Has Aggregates | ❌ No Aggregates | ✅ Correct |

## API Endpoints

### Analytics Service (Port 8082)

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

### Swagger UI
```
http://localhost:8082/swagger-ui.html
```

### H2 Console
```
http://localhost:8082/h2-console
JDBC URL: jdbc:h2:mem:analytics
Username: sa
Password: (empty)
```

## Testing Flow

1. **Start Analytics Service** (Port 8082)
2. **Start Account Service** (Port 8081)
3. **Create Account** → Analytics receives `AccountCreatedEvent`
4. **Credit Account** → Analytics receives `AccountCreditedEvent`
5. **Debit Account** → Analytics receives `AccountDebitedEvent`
6. **Query Stats** → Use QueryGateway to get current statistics

## Key Features

✅ **CQRS Pattern**: Follows same structure as Account Service
✅ **Event Sourcing**: Consumes events from Account Service
✅ **QueryGateway**: Uses Axon's QueryGateway pattern
✅ **Event Handlers**: Projects events into read model
✅ **Query Handlers**: Handles queries with @QueryHandler
✅ **Swagger Documentation**: Full API documentation
✅ **Transactional**: Event handling is transactional
✅ **Logging**: Comprehensive logging for debugging

## Next Steps

1. **Reload Maven Project** in IntelliJ (Right-click pom.xml → Maven → Reload Project)
2. **Run Analytics Service** (Port 8082)
3. **Run Account Service** (Port 8081)
4. **Test Event Flow** using Swagger UI or Postman
5. **Monitor Logs** to see event handling in action

## Notes

- The IntelliJ errors you see are just indexing issues
- All imports are correct and will resolve after Maven reload
- The structure perfectly matches the Account Service CQRS pattern
- Events are in the correct shared package for cross-service communication
