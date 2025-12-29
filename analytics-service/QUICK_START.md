# Analytics Service - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Prerequisites
- ✅ Java 17+ installed
- ✅ Maven installed (or use included mvnw)
- ✅ IntelliJ IDEA 2024.2.3
- ✅ Account Service running on port 8081

## Step 1: Open Project in IntelliJ

1. Open IntelliJ IDEA
2. File → Open → Navigate to `analytics-service` folder
3. Wait for IntelliJ to index the project

## Step 2: Reload Maven Dependencies

1. Right-click on `pom.xml`
2. Select **Maven** → **Reload Project**
3. Wait for dependencies to download (this resolves the import errors)

## Step 3: Run the Application

### Option A: Using IntelliJ
1. Navigate to `src/main/java/com/ilyasgrid/analyticsservice/AnalyticsServiceApplication.java`
2. Right-click on the file
3. Select **Run 'AnalyticsServiceApplication'**

### Option B: Using Maven
```bash
cd "D:/9raya/cycle/3 annee/Micro-Services/TP/Event-soucing-with-Axon/analytics-service"
./mvnw spring-boot:run
```

### Option C: Using Maven Wrapper (Windows)
```cmd
cd "D:\9raya\cycle\3 annee\Micro-Services\TP\Event-soucing-with-Axon\analytics-service"
mvnw.cmd spring-boot:run
```

## Step 4: Verify Service is Running

### Check Console Output
Look for these messages:
```
Started AnalyticsServiceApplication in X.XXX seconds
Tomcat started on port(s): 8082 (http)
```

### Access Swagger UI
Open browser: http://localhost:8082/swagger-ui.html

### Access H2 Console
Open browser: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:analytics`
- Username: `sa`
- Password: (leave empty)

## Step 5: Test the Integration

### 1. Create an Account (Account Service)
```http
POST http://localhost:8081/api/commands/accounts/create
Content-Type: application/json

{
  "initialBalance": 1000.0,
  "currency": "USD"
}
```

**Expected Response:**
```json
"account-id-here"
```

### 2. Check Analytics (Analytics Service)
```http
GET http://localhost:8082/queries/analytics/stats
```

**Expected Response:**
```json
{
  "id": "BANK_STATS_001",
  "totalBalance": 1000.0,
  "accountCount": 1
}
```

### 3. Credit the Account
```http
POST http://localhost:8081/api/commands/accounts/credit/{account-id}
Content-Type: application/json

{
  "amount": 500.0,
  "currency": "USD"
}
```

### 4. Verify Updated Analytics
```http
GET http://localhost:8082/queries/analytics/stats
```

**Expected Response:**
```json
{
  "id": "BANK_STATS_001",
  "totalBalance": 1500.0,
  "accountCount": 1
}
```

## 📊 Monitoring

### Watch the Logs
You should see messages like:
```
Handling AccountCreatedEvent for account: {id}
Updated stats - Total Balance: 1000.0, Account Count: 1

Handling AccountCreditedEvent for account: {id}
Updated stats after credit - Total Balance: 1500.0

Handling GetBankStatsQuery
```

### Check Database
1. Go to http://localhost:8082/h2-console
2. Connect with credentials above
3. Run query:
```sql
SELECT * FROM BANK_STATS;
```

Expected result:
```
ID              | TOTAL_BALANCE | ACCOUNT_COUNT
BANK_STATS_001  | 1500.0        | 1
```

## 🎯 API Endpoints

### Analytics Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/queries/analytics/stats` | Get current bank statistics |
| GET | `/swagger-ui.html` | API Documentation |
| GET | `/h2-console` | Database Console |
| GET | `/api-docs` | OpenAPI JSON |

### Account Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/commands/accounts/create` | Create new account |
| POST | `/api/commands/accounts/credit/{id}` | Credit account |
| POST | `/api/commands/accounts/debit/{id}` | Debit account |
| GET | `/queries/account/all` | Get all accounts |
| GET | `/queries/account/{id}` | Get account by ID |

## 🔧 Troubleshooting

### Port 8082 Already in Use
**Error:** `Port 8082 is already in use`

**Solution:** Change port in `application.properties`:
```properties
server.port=8083
```

### Events Not Received
**Problem:** Analytics not updating when accounts are created

**Checklist:**
1. ✅ Is Account Service running on port 8081?
2. ✅ Are both services using `axon.axonserver.enabled=false`?
3. ✅ Are event classes in the same package (`com.ilyasgrid.common_api.events`)?
4. ✅ Check logs for event handling messages

### Maven Dependencies Not Resolved
**Problem:** Import errors in IntelliJ

**Solution:**
1. Right-click `pom.xml`
2. Maven → Reload Project
3. File → Invalidate Caches → Invalidate and Restart

### H2 Console Not Accessible
**Problem:** Cannot access http://localhost:8082/h2-console

**Solution:** Verify in `application.properties`:
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 📚 Documentation Files

| File | Description |
|------|-------------|
| `README.md` | Comprehensive documentation |
| `IMPLEMENTATION_SUMMARY.md` | Complete implementation details |
| `ARCHITECTURE.md` | CQRS architecture diagrams |
| `CHECKLIST.md` | Implementation verification |
| `QUICK_START.md` | This file - Quick start guide |

## 🎓 Understanding the Flow

### Event Flow
```
1. User creates account in Account Service
   ↓
2. AccountAggregate publishes AccountCreatedEvent
   ↓
3. Event travels via Axon Event Bus
   ↓
4. AnalyticsEventHandler receives event
   ↓
5. BankStats entity is updated
   ↓
6. Changes saved to H2 database
```

### Query Flow
```
1. User requests GET /queries/analytics/stats
   ↓
2. AnalyticsQueryController receives request
   ↓
3. QueryGateway dispatches GetBankStatsQuery
   ↓
4. AnalyticsQueryHandler handles query
   ↓
5. BankStatsRepository fetches data
   ↓
6. BankStats returned to user
```

## 🏆 Success Criteria

You'll know everything is working when:

✅ Analytics Service starts on port 8082
✅ Swagger UI is accessible
✅ Creating an account updates analytics (count: 1)
✅ Crediting an account increases total balance
✅ Debiting an account decreases total balance
✅ Logs show event handling messages
✅ H2 database shows updated BANK_STATS table

## 🚦 Testing Checklist

- [ ] Start Account Service (8081)
- [ ] Start Analytics Service (8082)
- [ ] Access Swagger UI for both services
- [ ] Create account → Check analytics
- [ ] Credit account → Check analytics
- [ ] Debit account → Check analytics
- [ ] Verify logs show event handling
- [ ] Check H2 database for BANK_STATS

## 💡 Tips

1. **Use Swagger UI** for easy API testing
2. **Watch the logs** to see events being processed
3. **Check H2 Console** to verify database updates
4. **Test incrementally** - one operation at a time
5. **Compare with Account Service** to understand CQRS pattern

## 🎉 You're Ready!

The Analytics Service is now fully functional and following the CQRS pattern just like the Account Service. Happy coding! 🚀

---

**Need Help?**
- Check `README.md` for detailed documentation
- Review `ARCHITECTURE.md` for system design
- Verify `CHECKLIST.md` for implementation details
