# TUB-Projeto Comprehensive Analysis Report

**Generated**: May 31, 2026  
**Project**: TUB-Projeto (Transit Urban Bus Platform)  
**Framework**: Spring Boot 3.2.4 | Java 17 | PostgreSQL | Auth0

---

## Executive Summary

The TUB-Projeto is a **transit management platform** with core functionality for **alert management, vehicle/route scheduling, occupancy monitoring, and digital panel control**. However, it is **not yet equipped** with sophisticated data pipeline, monitoring, validation, and standardized API capabilities outlined in use cases 5.1-5.4.

| Feature | Status | Completeness |
|---------|--------|--------------|
| **5.1 - Data Ingestion Pipelines** | ❌ Missing | 0% |
| **5.2 - Data Flow Monitoring** | ⚠️ Partial | 30% |
| **5.3 - Vertical Integration Validation** | ❌ Missing | 0% |
| **5.4 - API Endpoints (NGSi-LD/MQTT/OpenAPI)** | ⚠️ Partial | 20% |

---

## Part 1: Project Structure

### Architecture Overview
```
TUB-Projeto (Spring Boot 3.2.4)
├── Controllers (7 REST controllers, 40+ endpoints)
├── Services (6 service classes)
├── Entities (9 core domain objects)
├── Repositories (8 data access layers)
├── Config (Security, DataInitializer)
└── Frontend (HTML/JS, 9 pages)
```

### Tech Stack
- **Backend**: Spring Boot 3.2.4, Java 17
- **Database**: PostgreSQL 192.168.233.121:5432 (tub_pgu)
- **Authentication**: Auth0 OAuth2 Resource Server (configured)
- **ORM**: Spring Data JPA + Hibernate
- **Frontend**: Static HTML/CSS/JavaScript with Leaflet maps
- **Port**: 8080

### Dependencies Summary
```xml
✓ spring-boot-starter-web          (REST API)
✓ spring-boot-starter-data-jpa     (ORM)
✓ spring-boot-starter-security     (Auth)
✓ spring-boot-starter-oauth2-resource-server  (OAuth2)
✓ postgresql                        (Database)
✓ lombok                            (Code generation)
✗ NO Kafka/RabbitMQ                (Messaging)
✗ NO Scheduled Tasks               (Async processing)
✗ NO Micrometer/Actuator          (Monitoring)
✗ NO Springdoc/Swagger            (API Docs)
```

---

## Part 2: Available Entities

### Core Domain Model

#### 1. Alert (Workflow-Centric)
```java
Entity: alerts table
Status Flow: ACTIVE → ACCEPTED → RESOLVED/PENDING
├── Identification: id, type, severity (HIGH|MEDIUM|LOW)
├── Content: description, source, metadata, recommendedActions
├── Timestamps: createdAt, acceptedAt, resolvedAt
├── Workflow: createdBy, acceptedBy, inconsistentReason
└── Purpose: Alert management with multi-role approval workflow
```

**Status Transitions**:
- Worker creates → ACTIVE
- Supervisor reviews → ACCEPTED (or returns PENDING with reason)
- Admin processes → RESOLVED or marks INCONSISTENT
- Return to PENDING if issues found

#### 2. Route
```java
Entity: routes table
├── Code (UNIQUE), Origin, Destination
├── Stops (ElementCollection - list of stop references)
├── Status: ACTIVE | INACTIVE | MAINTENANCE
├── Vehicles: 1:N relationship (OneToMany)
└── Metadata: flexible JSON storage
```

#### 3. Vehicle
```java
Entity: vehicles table
├── Plate (UNIQUE), Model, Capacity
├── Status: IN_SERVICE | OUT_OF_SERVICE | MAINTENANCE
├── Route: N:1 relationship with bidirectional management
└── Metadata: flexible JSON storage
```

#### 4. Stop
```java
Entity: stop table
├── Name, Latitude, Longitude
├── Operator: N:1 relationship
└── Purpose: Geographic waypoints for routes
```

#### 5. InformationPanel
```java
Entity: information_panel table
├── Location, Address, Installation Date
├── Manufacturer, Panel Type, Connectivity Type
├── Status, Last Updated timestamp
├── Predictions (JSON field for arrival predictions)
├── Stop & Route: N:1 relationships
└── Purpose: Digital display panels showing real-time transit info
```

#### 6. PassengerCount (Occupancy Data)
```java
Entity: passenger_counts table
├── PanelId, Line, Timestamp
├── EntryCount, ExitCount (raw counts)
├── Occupancy (calculated field via @PrePersist/@PreUpdate)
└── Purpose: Real-time occupancy tracking and analytics
```

#### 7. PanelMessage
```java
Entity: panel_message table
├── Title, Content, Priority
├── StartTime, EndTime, Status
├── TargetZone, MessageType
├── Panel: N:1 relationship
├── CreatedBy (Operator): N:1 relationship
└── Purpose: Dynamic messaging to transit users
```

#### 8. User
```java
Entity: users table
├── Username (UNIQUE), Password, Email
├── Role (string: ADMIN, USER, WORKER, SUPERVISOR)
├── Online status
└── Purpose: User authentication and role-based access
```

#### 9. Operator
```java
Entity: operator table
├── Name
└── Purpose: Transit operator organization
```

---

## Part 3: REST API Endpoints (40+ Endpoints)

### Alert Management (/api/alerts - 13 endpoints)

**Read Operations**:
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | Get all alerts |
| `/active` | GET | Get ACTIVE alerts (supervisor queue) |
| `/accepted` | GET | Get ACCEPTED alerts (admin queue) |
| `/pending` | GET | Get PENDING alerts (returned to supervisor) |
| `/my?username=` | GET | Get alerts created by user |
| `/worker` | GET | Get ACCEPTED alerts for workers |
| `/stats` | GET | Severity statistics (HIGH/MEDIUM/LOW counts) |
| `/stats/operational` | GET | Performance metrics: response time, resolution rate, inconsistency rate |
| `/history` | GET | Complete alert history |
| `/options` | GET | Available alert types and severity levels |

**Write Operations**:
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | POST | Create new alert |
| `/{id}/accept` | POST | Supervisor accepts alert (ACTIVE→ACCEPTED) |
| `/{id}/resolve` | POST | Admin resolves alert (ACCEPTED→RESOLVED) |
| `/{id}/inconsistent` | POST | Admin marks inconsistent (ACCEPTED→PENDING) |
| `/{id}/update-state-and-actions` | POST | Update status + recommended actions |

**Sample Alert Workflow**:
```
Worker creates: POST /api/alerts
                { type: "OCCUPANCY_HIGH", severity: "MEDIUM", source: "Panel-5" }
                ↓
Supervisor accepts: POST /api/alerts/{id}/accept?username=supervisor1
                ↓
Admin resolves: POST /api/alerts/{id}/resolve?username=admin1
```

---

### Panel Management (/api/panels - 11 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | List all panels |
| `/{id}` | GET | Get panel details |
| `/{id}/realtime` | GET | Get real-time panel info (predictions, ETA) |
| `/stop/{stopId}/realtime` | GET | Get real-time info for all panels at stop |
| `/{id}/messages` | GET | Get messages assigned to panel |
| `/stops` | GET | Get all stops |
| `/` | POST | Create new panel |
| `/create` | POST | Create panel (simplified) |
| `/{id}` | PUT | Update panel configuration |
| `/{id}` | DELETE | Delete panel |
| `/{id}/simulate-failure` | POST | Simulate panel failure (testing) |

**Real-Time Data Source**: Native SQL queries to GTFS tables
```sql
SELECT r.route_short_name, t.trip_headsign, st.arrival_time
FROM gtfs_stop_times st
JOIN gtfs_trips t ON st.trip_id = t.trip_id
JOIN gtfs_routes r ON t.route_id = r.route_id
WHERE st.stop_id = :stopId
ORDER BY st.arrival_time ASC LIMIT 5
```

---

### Passenger Occupancy (/api/passengers - 7 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | Get counts (filtered by panelId, from, to timestamps) |
| `/{panelId}/occupancy` | GET | Calculate occupancy for time range |
| `/latest` | GET | Get latest count per panel |
| `/live-status` | GET | Get current live occupancy |
| `/` | POST | Record passenger count entry |
| `/simulate/entry` | POST | Simulate passenger entry |
| `/simulate/exit` | POST | Simulate passenger exit |

**Query Example**:
```
GET /api/passengers?panelId=1&from=2026-05-31T08:00:00&to=2026-05-31T18:00:00
```

---

### Route Management (/api/routes - 7 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | List all routes |
| `/{id}` | GET | Get route by ID |
| `/by-code/{code}` | GET | Get route by code |
| `/` | POST | Create route |
| `/{id}` | PUT | Update route |
| `/{id}` | DELETE | Delete route |
| `/{id}/status` | POST | Change route status |

---

### Vehicle Management (/api/vehicles - 11 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | List all vehicles |
| `/{id}` | GET | Get vehicle by ID |
| `/plate/{plate}` | GET | Get vehicle by license plate |
| `/status/{status}` | GET | Filter vehicles by status |
| `/by-capacity` | GET | Find vehicles by capacity threshold |
| `/search/model` | GET | Search vehicles by model name |
| `/by-route` | GET | Find vehicles assigned to route |
| `/` | POST | Create vehicle |
| `/{id}` | PUT | Update vehicle |
| `/{id}` | DELETE | Delete vehicle |
| `/{id}/status` | POST | Change vehicle status |
| `/{id}/assign-route/{routeId}` | POST | Assign route to vehicle |
| `/{id}/assign-route-by-code` | POST | Assign route by route code |
| `/{id}/unassign-route` | POST | Remove route assignment |

---

### Stop Management (/api/stops - 5 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | List all stops |
| `/{id}` | GET | Get stop details |
| `/{id}/vehicles` | GET | Get vehicles currently at stop |
| `/` | POST | Create stop |
| `/{id}` | PUT | Update stop (name, coordinates) |
| `/{id}` | DELETE | Delete stop |

---

### Authentication (/api/auth - 2 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/login` | POST | User login (username, password) |
| `/logout` | POST | User logout (username) |

**Note**: Basic implementation; Auth0 OAuth2 configured but not fully integrated.

---

### Test Endpoint (/api/teste - 1 endpoint)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/paragens` | GET | Test endpoint listing stops |

---

## Part 4: Service Layer Architecture

### AlertService / AlertServiceImpl
```
Core Operations:
├── createAlert()              - Create new alert (default status: ACTIVE)
├── getActiveAlerts()          - Retrieve supervisor queue
├── getAcceptedAlerts()        - Retrieve admin queue
├── getPendingAlerts()         - Retrieve returned alerts
├── acceptAlert()              - Supervisor approves (ACTIVE→ACCEPTED)
├── resolveAlert()             - Admin completes (ACCEPTED→RESOLVED)
├── markAsInconsistent()       - Admin returns (ACCEPTED→PENDING + reason)
├── getSeverityStats()         - Count alerts by severity
├── getOperationalStats()      - Performance metrics
│   ├── avgResponseTimeSeconds
│   ├── avgResolutionTimeSeconds
│   ├── resolutionRate (%)
│   ├── inconsistencyRate (%)
│   └── inconsistentCount
└── getAlertOptions()          - Available types/severities
```

**Workflow State Machine**:
```
    [Worker] → POST /alerts
           ↓
    ACTIVE (Supervisor queue)
           ↓
    POST /accept
           ↓
    ACCEPTED (Admin queue) ↔ markAsInconsistent() → PENDING
           ↓
    POST /resolve
           ↓
    RESOLVED (Completed)
```

---

### PanelService
```
Core Operations:
├── getAllPanels()                 - List all information panels
├── getPanelById()                 - Get panel details
├── createPanel()                  - Create new panel
├── deletePanel()                  - Remove panel
├── getPanelRealTimeInfo()         - Get current predictions
├── getStopPanelsRealTimeInfo()    - Aggregate data for all panels at stop
│   ├── Native SQL to GTFS tables
│   ├── Arrival time calculations
│   ├── ETA computation (minutes)
│   └── Real-time occupancy per line
└── getAllStops()                  - Get stop list from GTFS
```

**Real-Time Data Integration**:
```
Request: GET /api/panels/{id}/realtime
         ↓
PanelService.getPanelRealTimeInfo()
         ↓
EntityManager.createNativeQuery()
         ↓
GTFS Tables (gtfs_stop_times, gtfs_trips, gtfs_routes)
         ↓
Response: { arrivals: [ { route, destination, eta_minutes }, ... ] }
```

---

### PassengerService
```
Core Operations:
├── save()                          - Record passenger count
├── getCounts()                     - Query by time range
├── calculateOccupancy()            - Sum entry-exit for period
├── getSummary()                    - Daily stats (entries, exits, balance)
├── getAllCounts()                  - List all records
├── getLatestCountsPerPanel()       - Latest count per panel
├── calculateCurrentOccupancy()     - Recent occupancy (last hour)
├── simulateEntry()                 - Create simulation record
└── simulateExit()                  - Create simulation record
```

---

### AuthService / AuthServiceImpl
```
Core Operations:
├── login()     - Basic login (username, password)
└── logout()    - Mark user as offline
```

**Limitation**: File-based; Auth0 OAuth2 configured in SecurityConfig but not fully integrated.

---

## Part 5: Data Flow Analysis

### Alert Workflow (Complete)
```
┌─────────────────────────────────────────────────────────────┐
│ Worker                                                       │
│ Creates Alert: POST /api/alerts                             │
│ { type: "CAPACITY_EXCEEDED", severity: "HIGH", source: "P5"}│
└────────────────┬────────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────────────┐
│ Database: INSERT INTO alerts (status='ACTIVE', ...)         │
└────────────────┬────────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────────────┐
│ Supervisor                                                   │
│ GET /api/alerts/active → Lists all ACTIVE alerts           │
│ Reviews context, selects action                            │
│   ├─ Accept: POST /{id}/accept                             │
│   │  → status='ACCEPTED', acceptedBy='supervisor1'          │
│   │  → acceptedAt=NOW                                      │
│   │  → recommendedActions cleared                          │
│   └─ (No endpoint for direct rejection; goes PENDING)      │
└────────────────┬────────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────────────┐
│ Admin                                                        │
│ GET /api/alerts/accepted → Lists all ACCEPTED alerts       │
│   ├─ Resolve: POST /{id}/resolve                           │
│   │  → status='RESOLVED', resolvedAt=NOW                   │
│   │  → Alert closed successfully                           │
│   └─ Return: POST /{id}/inconsistent?reason=               │
│      → status='PENDING', inconsistentReason='...'          │
│      → acceptedBy=NULL (returns to supervisor)             │
└────────────────┬────────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────────────┐
│ Data: RESOLVED alert → Historical record                    │
│ Queried by: GET /api/alerts/history or /api/alerts         │
│ Analyzed by: GET /api/alerts/stats/operational             │
└─────────────────────────────────────────────────────────────┘
```

### Occupancy Monitoring Flow (Complete)
```
┌──────────────────────┐
│ Information Panel    │
│ (Hardware sensor)    │
└─────────┬────────────┘
          ↓ Detects entry/exit
┌──────────────────────────────────────────────┐
│ POST /api/passengers                          │
│ { panelId: 5, entryCount: 3, exitCount: 1 } │
└─────────┬──────────────────────────────────────┘
          ↓
┌──────────────────────────────────────────────┐
│ PassengerService.save()                      │
│ ├─ @PrePersist calculates occupancy          │
│ │  = entryCount - exitCount                  │
│ └─ Save to passenger_counts table            │
└─────────┬──────────────────────────────────────┘
          ↓
┌──────────────────────────────────────────────┐
│ Query: GET /api/passengers/live-status       │
│ Returns latest counts per panel              │
│ Used by: Panel display, supervisor dashboard│
└──────────────────────────────────────────────┘
```

### Real-Time Panel Data Flow (Complete)
```
┌─────────────────────────────────────┐
│ Request: GET /api/panels/5/realtime │
└────────────────┬────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────┐
│ PanelService.getPanelRealTimeInfo(5)                │
└────────────────┬────────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────────────────┐
│ EntityManager.createNativeQuery()                            │
│ SELECT r.route_short_name, t.trip_headsign,                 │
│        st.arrival_time, st.stop_id                          │
│ FROM gtfs_stop_times st                                      │
│ JOIN gtfs_trips t ON st.trip_id = t.trip_id                 │
│ JOIN gtfs_routes r ON t.route_id = r.route_id               │
│ WHERE st.stop_id = {panelStopId}                            │
│ ORDER BY st.arrival_time ASC LIMIT 5                        │
└────────────────┬───────────────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────────────────┐
│ GTFS PostgreSQL Tables                                       │
│ (TUB Transit Authority data)                                 │
└────────────────┬───────────────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────────────────┐
│ Response: {                                                   │
│   "arrivals": [                                              │
│     {                                                         │
│       "routeName": "LINHA-1",                                │
│       "routeDescription": "Centro → Aeroporto",              │
│       "arrivalTime": "14:35",                                │
│       "etaMinutes": 8                                        │
│     },                                                        │
│     { "routeName": "LINHA-2", ... },                         │
│     ...                                                       │
│   ]                                                           │
│ }                                                             │
└──────────────────────────────────────────────────────────────┘
```

---

## Part 6: Current Implementation Status

### ✅ IMPLEMENTED & WORKING

| Feature | Implementation | Status |
|---------|-----------------|--------|
| **Alert Management** | Full workflow ACTIVE→ACCEPTED→RESOLVED/PENDING | ✅ Complete |
| **Route CRUD** | Create, read, update, delete routes | ✅ Complete |
| **Vehicle CRUD** | Create, read, update, delete, assign routes | ✅ Complete |
| **Stop CRUD** | Create, read, update, delete stops | ✅ Complete |
| **Panel CRUD** | Create, read, update, delete information panels | ✅ Complete |
| **Occupancy Recording** | Record entry/exit counts, calculate occupancy | ✅ Complete |
| **Real-Time GTFS Integration** | Query GTFS tables for arrival predictions | ✅ Complete |
| **Basic Authentication** | Login/logout with role-based user model | ✅ Complete |
| **Statistics** | Alert severity stats, operational performance metrics | ✅ Complete |
| **Panel Messages** | Create and manage messages for panels | ✅ Complete |

---

### ⚠️ PARTIALLY IMPLEMENTED

| Feature | Current State | Gap |
|---------|---------------|-----|
| **Auth0 Integration** | OAuth2 Resource Server configured in SecurityConfig | Not fully wired into controllers; using basic auth |
| **Data Flow Monitoring** | Alert timestamps tracked (createdAt, acceptedAt, resolvedAt) | No centralized pipeline monitoring; no event logging |
| **Occupancy Monitoring** | Entry/exit counts recorded with timestamps | No real-time stream; no anomaly detection |
| **Real-Time Data** | GTFS queries available; ETA calculations present | Limited to stop-level; no cross-system monitoring |

---

### ❌ NOT IMPLEMENTED

#### 5.1 Data Ingestion Pipelines
```
Missing Components:
├── ❌ @Scheduled tasks / scheduled batch jobs
├── ❌ Message queue (Kafka, RabbitMQ, ActiveMQ)
├── ❌ ETL/data transformation layer
├── ❌ Async data processing
├── ❌ Data connectors (API polling, webhooks, file ingestion)
├── ❌ Change Data Capture (CDC)
├── ❌ Event streaming
└── ❌ Batch file uploads / bulk data imports

How Data Currently Enters System:
1. Manual API calls (POST /api/alerts, POST /api/passengers)
2. Direct database inserts
3. Frontend-triggered operations
4. No automated data flow
```

#### 5.2 Data Flow Monitoring
```
Missing Components:
├── ❌ Metrics collection (Micrometer, Prometheus)
├── ❌ Health checks (Spring Boot Actuator)
├── ❌ Data lineage tracking
├── ❌ Event logging / audit trail
├── ❌ Pipeline status dashboards
├── ❌ Real-time monitoring UI
├── ❌ Anomaly detection
├── ❌ SLA monitoring
└── ❌ Alert escalation on failures

What IS Available:
├── ✅ Alert status transitions (manual, in DB)
├── ✅ Operational stats (avg response time, resolution rate)
├── ✅ Occupancy time-series data (passenger counts)
└── ✅ GTFS arrival predictions
```

#### 5.3 Vertical Integration Validation
```
Missing Components:
├── ❌ Cross-entity consistency checks
├── ❌ Data quality framework
├── ❌ Business rule validation engine
├── ❌ Reconciliation service
├── ❌ Transformation validation
├── ❌ Master data management
├── ❌ Reference data governance
└── ❌ Exception handling for data quality issues

Examples of Missing Validations:
- Route must have active vehicles before transitioning to ACTIVE
- Vehicle capacity must match route requirements
- Panel location must be near a valid stop
- Occupancy cannot exceed vehicle capacity
- Time series consistency (occupancy must be monotonic with entry/exit)
- Cross-system entity reconciliation (internal stops vs GTFS stops)
```

#### 5.4 API Endpoints for Data Querying (NGSi-LD, MQTT, OpenAPI)
```
Missing Components:

1. NGSi-LD Support:
   ├── ❌ NGSI-LD entity model (@context, @type)
   ├── ❌ Linked Data context (JSON-LD)
   ├── ❌ NGSI-LD query language support
   ├── ❌ Temporal queries
   ├── ❌ Relationship traversal
   └── ❌ Semantic enrichment

2. MQTT Support:
   ├── ❌ MQTT broker connection
   ├── ❌ Spring Cloud Stream integration
   ├── ❌ Publish/subscribe handlers
   ├── ❌ Message queue for real-time events
   ├── ❌ Topic management
   └── ❌ QoS configuration

3. OpenAPI/Swagger Support:
   ├── ❌ Springdoc-openapi library
   ├── ❌ API documentation generation
   ├── ❌ Interactive Swagger UI (/swagger-ui.html)
   ├── ❌ API versioning
   ├── ❌ Response schema documentation
   └── ❌ Example requests/responses

4. Additional API Standards:
   ├── ❌ Standardized error responses (RFC 7807)
   ├── ❌ Pagination support (limit, offset, cursor)
   ├── ❌ Filtering language (e.g., cqfilter)
   ├── ❌ Sorting support (orderBy parameter)
   ├── ❌ Rate limiting
   ├── ❌ Request/response compression
   └── ❌ Batch operations

Current API State:
├── ✅ RESTful endpoints (40+ endpoints)
├── ✅ JSON request/response
├── ✅ Basic CRUD operations
├── ✅ Business logic endpoints (accept, resolve, etc.)
└── ❌ Standard documentation
   ❌ Advanced query capabilities
   ❌ Real-time event streaming
```

---

## Part 7: Database Schema Overview

### Tables (9 core + GTFS reference)

```sql
-- Core Application Tables

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  role VARCHAR(50),
  is_online BOOLEAN DEFAULT false
);

CREATE TABLE operator (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255)
);

CREATE TABLE stop (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  operator_id BIGINT REFERENCES operator(id)
);

CREATE TABLE routes (
  id SERIAL PRIMARY KEY,
  code VARCHAR(255) UNIQUE NOT NULL,
  origin VARCHAR(255),
  destination VARCHAR(255),
  status VARCHAR(50) DEFAULT 'ACTIVE',
  metadata TEXT
);

CREATE TABLE route_stops (
  route_id BIGINT REFERENCES routes(id),
  stop VARCHAR(255)
);

CREATE TABLE vehicles (
  id SERIAL PRIMARY KEY,
  plate VARCHAR(255) UNIQUE NOT NULL,
  model VARCHAR(255),
  status VARCHAR(50) DEFAULT 'IN_SERVICE',
  capacity INTEGER,
  route_id BIGINT REFERENCES routes(id),
  metadata TEXT
);

CREATE TABLE information_panel (
  id SERIAL PRIMARY KEY,
  location VARCHAR(255),
  address VARCHAR(255),
  installation_date TIMESTAMP,
  manufacturer VARCHAR(255),
  panel_type VARCHAR(255),
  status VARCHAR(50),
  connectivity_type VARCHAR(50),
  stop_id BIGINT REFERENCES stop(id),
  route_id BIGINT REFERENCES routes(id),
  predictions TEXT,
  last_updated TIMESTAMP
);

CREATE TABLE alerts (
  id SERIAL PRIMARY KEY,
  type VARCHAR(255),
  description TEXT,
  source VARCHAR(255),
  severity VARCHAR(50),
  status VARCHAR(50) DEFAULT 'ACTIVE',
  created_at TIMESTAMP,
  accepted_at TIMESTAMP,
  resolved_at TIMESTAMP,
  metadata TEXT,
  created_by VARCHAR(255),
  recommended_actions TEXT,
  accepted_by VARCHAR(255),
  inconsistent_reason TEXT
);

CREATE TABLE passenger_counts (
  id SERIAL PRIMARY KEY,
  panel_id BIGINT,
  line VARCHAR(255),
  timestamp TIMESTAMP,
  entry_count INTEGER,
  exit_count INTEGER,
  occupancy INTEGER
);

CREATE TABLE panel_message (
  id SERIAL PRIMARY KEY,
  title VARCHAR(255),
  content TEXT,
  priority INTEGER,
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  status VARCHAR(50),
  target_zone VARCHAR(255),
  message_type VARCHAR(50),
  panel_id BIGINT REFERENCES information_panel(id),
  created_by_id BIGINT REFERENCES operator(id)
);

-- GTFS Reference Tables (External - TUB Transit Authority)
-- gtfs_routes, gtfs_stops, gtfs_stop_times, gtfs_trips, etc.
-- (Accessed via native SQL queries from PanelService)
```

---

## Part 8: Missing Implementations Detailed

### 5.1 Data Ingestion Pipelines - Specific Gaps

#### Current State
- Manual data entry via REST API
- Frontend triggers operations
- No automation
- No background processing

#### Required for 5.1
```java
// MISSING: @EnableScheduling configuration
// @EnableAsync support

// MISSING: Scheduled Tasks
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void syncGTFSData() {
    // Fetch latest GTFS data from TUB
    // Update database
}

@Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
public void aggregateDailyOccupancyData() {
    // Aggregate passenger counts
}

// MISSING: Message Queue Integration (e.g., Kafka)
@KafkaListener(topics = "vehicle-positions")
public void processVehiclePosition(VehiclePosition pos) {
    // Real-time vehicle tracking
}

// MISSING: Batch Processors
@Bean
public Job importAlertsJob(Step importStep) { }

// MISSING: API Connectors
public class GTFSApiConnector {
    public void pollGTFSEndpoint() { }
}

// MISSING: File Ingestion
@PostMapping("/import/occupancy-data")
public void importOccupancyCSV(MultipartFile file) { }
```

#### Technology Stack Needed
```
├── Spring Cloud Task
├── Spring Batch
├── Kafka / Spring Cloud Stream
├── Scheduler Service (Quartz or similar)
├── Message Queue (RabbitMQ, Kafka, ActiveMQ)
├── ETL Tool (Talend, Pentaho, or custom)
├── Data transformation library (MapStruct, ModelMapper)
└── File processing (Apache POI, OpenCSV)
```

---

### 5.2 Data Flow Monitoring - Specific Gaps

#### Current State
- Alert timestamps recorded
- Manual stat queries available
- No real-time visibility
- No pipeline health monitoring

#### Required for 5.2
```java
// MISSING: Metrics Collection
@Autowired
private MeterRegistry meterRegistry;

public void recordAlertMetric(Alert alert) {
    meterRegistry.counter("alerts.created", "severity", alert.getSeverity()).increment();
    meterRegistry.timer("alert.resolution.time").record(duration);
}

// MISSING: Event Logging
@PostMapping("/alerts")
public Alert create(@RequestBody Alert alert) {
    auditLog.log("ALERT_CREATED", alert.getId(), alert.getSource());
    return alertService.createAlert(alert);
}

// MISSING: Health Checks
@Component
public class DataPipelineHealth implements HealthIndicator {
    @Override
    public Health health() {
        // Check if GTFS sync is recent
        // Check if passenger count receiver is active
        return Health.status("UP").build();
    }
}

// MISSING: Real-time Dashboards
// Need: Grafana + Prometheus + Custom WebSocket streams
// Or: Spring Boot Admin + Custom Actuator endpoints

// MISSING: Alerting on Failures
public void monitorPipelineHealth() {
    if (lastGTFSSyncTime > 15 minutes ago) {
        sendAlert("GTFS sync failed");
    }
}
```

#### Technology Stack Needed
```
├── Micrometer (metrics)
├── Prometheus (metrics storage)
├── Grafana (visualization)
├── Spring Boot Actuator (health checks)
├── ELK Stack / Splunk (logging & analytics)
├── Custom WebSocket endpoints (real-time UI)
├── Alerting service (email, Slack, etc.)
└── Audit logging framework
```

---

### 5.3 Vertical Integration Validation - Specific Gaps

#### Current State
- CRUD operations only
- No cross-entity validation
- No data quality checks
- No reconciliation logic

#### Required for 5.3
```java
// MISSING: Consistency Validation Service
@Service
public class DataConsistencyService {
    
    public ValidationResult validateRoute(Route route) {
        // 1. Check: All referenced stops must exist
        // 2. Check: All assigned vehicles must have valid capacity
        // 3. Check: Status transitions must be legal
        // 4. Check: Metadata structure must be valid
        return result;
    }
    
    public ValidationResult validateOccupancy(PassengerCount count) {
        // 1. Occupancy must be = entryCount - exitCount
        // 2. Total occupancy <= vehicle capacity
        // 3. Time series consistency (occupancy shouldn't drop unexpectedly)
        // 4. Panel must exist and be active
        return result;
    }
    
    public void reconcileStopsWithGTFS() {
        // Fetch all stops from GTFS
        // Compare with local stops table
        // Flag missing or orphaned records
        // Create reconciliation report
    }
}

// MISSING: Business Rule Engine
@Service
public class BusinessRuleValidator {
    
    public void validateTransition(Alert alert, String newStatus) {
        // ACTIVE → ACCEPTED: allowed
        // ACCEPTED → PENDING: only if reason provided
        // PENDING → ACTIVE: forbidden
        // etc.
    }
}

// MISSING: Master Data Management
@Service
public class MasterDataService {
    public Stop getReferenceStop(String externalId) {
        // Look up canonical stop definition
        // Handle aliases, deduplication
        // Return master record
    }
}

// MISSING: Reference Data Governance
@Entity
public class StopMaster {
    private String internalId;
    private String gtfsId;    // Link to GTFS
    private String status;    // ACTIVE, DEPRECATED, MERGED
    private Set<Stop> aliases; // Related local stops
}
```

#### Validation Categories
```
Entity-Level Validation:
├── Referential integrity (foreign keys)
├── Constraints (unique, not null, range checks)
├── Type validation (string format, enum values)
└── Computed field validation (e.g., occupancy calculation)

Cross-Entity Validation:
├── Stop must exist when creating Route with stops
├── Vehicle capacity must match route capacity class
├── Panel must be associated with existing Stop or Route
├── Alert source must reference valid entity
└── User role must have permissions for action

System-Level Validation:
├── GTFS stops vs Local stops reconciliation
├── Occupancy data vs Vehicle capacity correlation
├── Alert workflow state machine enforcement
├── Time series data consistency
└── Duplicate detection (same vehicle, stop, or route)

Quality Rules:
├── Outlier detection (occupancy > capacity)
├── Time-series anomalies (missing data)
├── Data freshness (updates not stale)
├── Geographic consistency (coordinates within bounds)
└── Completeness checks (required fields populated)
```

#### Technology Stack Needed
```
├── Drools or Easy Rules (rule engine)
├── Apache Commons Validator (validation utilities)
├── Flyway / Liquibase (schema governance)
├── Vérifier or similar (data quality tool)
├── Custom validation framework
└── Event-driven validation pipeline
```

---

### 5.4 API Standardization - Specific Gaps

#### Current State
- REST endpoints available
- Basic JSON payload
- No OpenAPI documentation
- No NGSi-LD or MQTT support

#### A. OpenAPI/Swagger Support

```java
// MISSING: Springdoc Configuration (pom.xml)
// <dependency>
//   <groupId>org.springdoc</groupId>
//   <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
//   <version>2.0.2</version>
// </dependency>

// MISSING: Endpoint Documentation
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alert Management", description = "Alert workflow and lifecycle")
public class AlertController {
    
    @GetMapping
    @Operation(summary = "Get all alerts", description = "Returns paginated list of all alerts")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AlertDTO.class)))
    public List<Alert> getAll() { }
}

// Auto-generates: /swagger-ui.html, /v3/api-docs
```

#### B. NGSi-LD Support

```java
// MISSING: NGSI-LD Context
// Could return alerts as:
{
  "@context": "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld",
  "@type": "https://schema.example.com/Alert",
  "id": "urn:ngsi-ld:Alert:12345",
  "type": "Alert",
  "severity": {
    "type": "Property",
    "value": "HIGH"
  },
  "source": {
    "type": "Relationship",
    "object": "urn:ngsi-ld:Panel:789"
  },
  "createdAt": {
    "type": "Property",
    "value": "2026-05-31T10:30:00Z"
  }
}

// Requires:
// - Spring Linked Data library
// - Context serialization (@JsonLD)
// - Query language support (SPARQL-like)
// - Temporal queries
```

#### C. MQTT Support

```java
// MISSING: MQTT Broker Configuration
@Configuration
@EnableIntegration
public class MqttConfig {
    
    @Bean
    public IntegrationFlow mqttInbound() {
        return IntegrationFlows
            .from(Mqtt.inboundAdapter("tcp://localhost:1883", "client-id",
                "tub/alerts", "tub/occupancy"))
            .handle(this::handleMqttMessage)
            .get();
    }
    
    private void handleMqttMessage(Message<?> msg) {
        // Process: tub/alerts → AlertService.createAlert()
        // Process: tub/occupancy → PassengerService.save()
    }
}

// Needs:
// ├── MQTT Broker (Mosquitto, HiveMQ, etc.)
// ├── Spring Integration Framework
// ├── Message marshalling (JSON payload)
// └── Error handling / dead letter queue
```

#### D. Advanced Query Support

```java
// MISSING: Pagination
@GetMapping("/alerts")
public Page<Alert> getAlerts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    Pageable pageable) {
    return alertRepository.findAll(pageable);
}

// MISSING: Filtering
@GetMapping("/alerts/search")
public List<Alert> searchAlerts(
    @RequestParam(required = false) String severity,
    @RequestParam(required = false) String status) {
    // /api/alerts/search?severity=HIGH&status=ACTIVE
}

// MISSING: Sorting
@GetMapping("/alerts")
public List<Alert> getAlerts(
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "DESC") String direction) {
}

// MISSING: Complex Queries (CQFilter)
// /api/alerts?$filter=severity eq 'HIGH' and status eq 'ACTIVE'
// Requires: QueryDSL, Spring Data REST, or custom parser
```

#### E. Error Response Standardization (RFC 7807)

```java
// MISSING: Problem Response Format
{
  "type": "https://api.tub.pt/problems/resource-not-found",
  "title": "Alert Not Found",
  "status": 404,
  "detail": "Alert with ID 999 does not exist",
  "instance": "/api/alerts/999",
  "timestamp": "2026-05-31T10:35:00Z"
}

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage()));
    }
}
```

#### Technology Stack Needed for 5.4
```
OpenAPI:
├── springdoc-openapi (Maven)
├── Swagger UI (auto-included)
└── OpenAPI 3.0.3 spec generation

NGSi-LD:
├── JSON-LD library
├── Linked Data vocabulary
├── SPARQL query support (optional)
└── Semantic ontology alignment

MQTT:
├── MQTT Broker (Mosquitto/HiveMQ)
├── Spring Integration
├── spring-integration-mqtt
└── Message payload marshalling

Advanced Queries:
├── Spring Data REST (HAL support)
├── QueryDSL or Querydsl
├── Specification API
└── Custom filter parser

Error Handling:
├── RFC 7807 ProblemDetail (Spring 6.0+)
├── Jackson for serialization
└── Custom exception mapping
```

---

## Part 9: Integration Landscape

### Current Integrations
```
┌──────────────────────────────────┐
│ TUB-Projeto Backend              │
├──────────────────────────────────┤
│ ✅ PostgreSQL (tub_pgu DB)       │
│ ✅ GTFS Data (Native SQL)        │
│ ✅ Auth0 (Configured, basic use) │
│ ✅ Spring Security               │
└──────────────────────────────────┘
```

### Missing Integrations for 5.1-5.4
```
Real-Time Data Sources:
├── ❌ MQTT Broker (for vehicle positions, sensor data)
├── ❌ Message Queue (Kafka, RabbitMQ for event streaming)
├── ❌ API Polling Service (external data providers)
└── ❌ File/Batch Ingestion Pipeline

Data Processing:
├── ❌ ETL Engine (Talend, Apache NiFi, or custom)
├── ❌ Stream Processing (Apache Kafka Streams, Flink)
├── ❌ Batch Framework (Spring Batch)
└── ❌ Async Task Processing (Spring @Async, Quartz)

Monitoring & Observability:
├── ❌ Prometheus (metrics collection)
├── ❌ Grafana (visualization)
├── ❌ ELK Stack (logging & analytics)
├── ❌ Jaeger (distributed tracing)
└── ❌ Custom health check service

Standards & APIs:
├── ❌ NGSi-LD server
├── ❌ OpenAPI/Swagger UI
├── ❌ GraphQL endpoint (optional)
└── ❌ WebSocket for real-time updates

Master Data:
├── ❌ MDM (Master Data Management) system
├── ❌ Reference data service
└── ❌ Reconciliation engine
```

---

## Part 10: Recommendations for Implementation

### Priority 1: Data Ingestion (5.1)

**Goal**: Enable automated data flow into the system

```java
// Step 1: Add dependencies
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

// Step 2: Create scheduled task for GTFS sync
@Service
@EnableScheduling
public class GTFSDataSyncService {
    @Scheduled(fixedDelay = 600000) // 10 minutes
    public void syncGTFSData() {
        List<RouteDTO> freshRoutes = gtfsClient.fetchRoutes();
        routeRepository.saveAll(convert(freshRoutes));
    }
}

// Step 3: Create Kafka consumer for occupancy data
@Service
public class OccupancyDataConsumer {
    @KafkaListener(topics = "occupancy-events")
    public void consume(OccupancyEvent event) {
        PassengerCount count = new PassengerCount();
        count.setPanelId(event.getPanelId());
        count.setEntryCount(event.getEntries());
        passengerService.save(count);
    }
}

// Step 4: Create batch importer for bulk data
@Component
public class OccupancyBatchImporter {
    @PostMapping("/import/occupancy")
    public ResponseEntity<String> importCSV(@RequestParam MultipartFile file) {
        // Parse CSV, validate, insert in batch
    }
}
```

---

### Priority 2: Data Flow Monitoring (5.2)

**Goal**: Visibility into data pipeline health and performance

```java
// Step 1: Add Micrometer/Prometheus
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

// Step 2: Create metrics for data flow
@Service
public class DataFlowMetricsService {
    @Autowired private MeterRegistry meterRegistry;
    
    public void recordAlertIngestion(Alert alert) {
        meterRegistry.counter("alerts.ingested", 
            "severity", alert.getSeverity()).increment();
    }
    
    public void recordOccupancyUpdate(PassengerCount count) {
        meterRegistry.timer("occupancy.update.time")
            .record(() -> passengerService.save(count));
    }
}

// Step 3: Create custom health indicators
@Component
public class DataPipelineHealth implements HealthIndicator {
    @Override
    public Health health() {
        LocalDateTime lastSync = syncService.getLastSyncTime();
        if (Duration.between(lastSync, LocalDateTime.now()).toMinutes() > 15) {
            return Health.down()
                .withDetail("lastSync", lastSync)
                .build();
        }
        return Health.up().build();
    }
}

// Exposes: /actuator, /actuator/metrics, /actuator/prometheus
// Scrape with: curl http://localhost:8080/actuator/prometheus
```

---

### Priority 3: Vertical Integration Validation (5.3)

**Goal**: Ensure data consistency across entities

```java
// Create Validation Service
@Service
public class VerticalIntegrationValidator {
    
    @Autowired private RouteRepository routeRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private StopRepository stopRepository;
    
    public ValidationResult validateRoute(Route route) {
        List<String> errors = new ArrayList<>();
        
        // 1. Check stops exist
        for (String stopName : route.getStops()) {
            if (!stopRepository.existsByName(stopName)) {
                errors.add("Stop not found: " + stopName);
            }
        }
        
        // 2. Check vehicles have sufficient capacity
        for (Vehicle vehicle : vehicleRepository.findByRoute(route)) {
            if (vehicle.getCapacity() < minimumRouteCapacity(route)) {
                errors.add("Vehicle " + vehicle.getPlate() + " insufficient capacity");
            }
        }
        
        // 3. Check status transitions are legal
        // ... etc
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public void validateOccupancy(PassengerCount count) {
        InformationPanel panel = panelRepository.findById(count.getPanelId());
        if (panel == null) throw new ValidationException("Panel not found");
        
        if (count.getOccupancy() > getVehicleCapacity(panel)) {
            throw new ValidationException("Occupancy exceeds vehicle capacity");
        }
    }
}

// Integrate into workflow
@PostMapping("/alerts")
public Alert createAlert(@RequestBody Alert alert) {
    validator.validateAlert(alert);
    return alertService.createAlert(alert);
}
```

---

### Priority 4: API Standardization (5.4)

#### A. Add OpenAPI Documentation

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

```java
// OpenAPI Configuration
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TUB-Projeto API")
                .version("1.0.0")
                .description("Transit Urban Bus Management Platform")
                .termsOfService("http://tub.pt/terms"));
    }
}

// Add to Controller
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Alert management endpoints")
public class AlertController {
    
    @GetMapping
    @Operation(summary = "Get all alerts")
    @ApiResponse(responseCode = "200", description = "List of alerts")
    public List<Alert> getAll() { }
    
    @PostMapping
    @Operation(summary = "Create new alert")
    @ApiResponse(responseCode = "201", description = "Alert created")
    public Alert create(@RequestBody Alert alert) { }
}

// Exposes: http://localhost:8080/swagger-ui.html
```

#### B. Add MQTT Support

```xml
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-mqtt</artifactId>
</dependency>
```

```java
@Configuration
@EnableIntegration
public class MqttConfig {
    
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { "tcp://localhost:1883" });
        factory.setConnectionOptions(options);
        return factory;
    }
    
    @Bean
    public IntegrationFlow mqttInbound() {
        return IntegrationFlows
            .from(Mqtt.inboundAdapter("tcp://localhost:1883", "tub-app",
                "tub/alerts/+", "tub/occupancy/+"))
            .handle(new MqttMessageHandler())
            .get();
    }
}

public class MqttMessageHandler implements MessageHandler {
    @Override
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        
        if (topic.startsWith("tub/alerts")) {
            processAlert(payload);
        } else if (topic.startsWith("tub/occupancy")) {
            processOccupancy(payload);
        }
    }
}
```

---

## Part 11: Summary Matrix

### Capability Assessment

| Capability | Current | Needed | Complexity |
|------------|---------|--------|------------|
| **5.1.1 ETL Pipelines** | None | Full | HIGH |
| **5.1.2 Scheduled Ingestion** | None | Full | MEDIUM |
| **5.1.3 Message Queues** | None | Full | HIGH |
| **5.1.4 Batch Processing** | None | Full | MEDIUM |
| **5.2.1 Data Lineage** | None | Full | HIGH |
| **5.2.2 Flow Monitoring** | Partial | Enhanced | MEDIUM |
| **5.2.3 Metrics Collection** | None | Full | MEDIUM |
| **5.2.4 Health Checks** | None | Full | LOW |
| **5.3.1 Consistency Validation** | None | Full | HIGH |
| **5.3.2 Data Quality** | None | Full | MEDIUM |
| **5.3.3 Reconciliation** | None | Full | HIGH |
| **5.4.1 OpenAPI Docs** | None | Full | LOW |
| **5.4.2 NGSi-LD Support** | None | Full | HIGH |
| **5.4.3 MQTT Messaging** | None | Full | MEDIUM |
| **5.4.4 Advanced Queries** | None | Full | MEDIUM |

---

## Part 12: Conclusion

### Current State
The TUB-Projeto is a **well-structured Spring Boot application** with solid foundational features:
- ✅ Complete alert lifecycle management
- ✅ Vehicle/route/stop CRUD operations
- ✅ Real-time GTFS data integration
- ✅ Occupancy tracking
- ✅ Role-based user management
- ✅ 40+ REST endpoints

### Readiness for UC 5.1-5.4
- **5.1 (Data Ingestion)**: 0% ready - No pipelines, no async processing
- **5.2 (Data Flow Monitoring)**: 30% ready - Basic alert tracking, needs comprehensive monitoring
- **5.3 (Vertical Integration)**: 0% ready - No validation framework, no reconciliation
- **5.4 (API Standards)**: 20% ready - REST endpoints exist, no OpenAPI/NGSi-LD/MQTT

### Next Steps
1. **Short-term (1-2 months)**: Implement OpenAPI documentation + basic MQTT adapter
2. **Medium-term (2-3 months)**: Add Prometheus metrics + health checks + batch ingestion
3. **Long-term (3-6 months)**: Build validation framework + NGSi-LD support + full stream processing

---

**Report Generated**: May 31, 2026  
**Analyzed By**: Copilot  
**Repository**: \\wsl.localhost\Ubuntu\home\simaocosta06\TUB-Projeto
