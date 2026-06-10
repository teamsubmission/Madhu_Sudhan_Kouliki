# Policy Overview BFF

A Backend-for-Frontend (BFF) service for Chubb's APAC operations. It sits between the
Policy Overview Dashboard (Angular) and the core policy datastore, aggregating and
transforming policy data into a frontend-friendly, paginated shape.

---

## 1. Project Overview

The Policy Overview Dashboard needs a tailored API that lists insurance policies with
server-side pagination, filtering, sorting and free-text search, exposes a single-policy
detail view, supports flagging policies for review, and provides aggregate summary
statistics.

Core capabilities:

- **List policies** with pagination, sorting, multi-field filtering and free-text search.
- **Retrieve a single policy** by its identifier.
- **Flag policies** for review in bulk.
- **Summary metrics** — counts by status, premium totals by line of business, and a
  count of policies expiring soon.

Regions in scope: Singapore, Hong Kong, Australia, Japan, Thailand, Indonesia, Malaysia,
Philippines.

---

## 2. Architecture Overview

The service follows a **layered Clean Architecture** with a framework-free domain core.
Dependencies point inward; only **domain models** cross layer boundaries, and each
boundary has an explicit mapper.

```
            ┌─────────────────────────────────────────────┐
   HTTP ───▶│  api        controllers, DTOs, mappers        │
            │   │         (request/response shapes)          │
            │   ▼                                            │
            │  service    use cases / business orchestration │
            │   │                                            │
            │   ▼                                            │
            │  domain     pure models, enums, exceptions     │◀── no framework deps
            │   ▲                                            │
            │   │                                            │
            │  infrastructure  JPA entities, repositories,   │
            │                  specifications, mappers       │
            └─────────────────────────────────────────────┘
```

- **DTOs never leave `api`**; **JPA entities never leave `infrastructure`**.
- The flow for a read is: `Entity → (EntityToDomain) → domain Policy → (DomainToResponseDto) → ResponseDto`.
- Dynamic filtering is implemented with **JPA Specifications**, composed null-safely from
  a `PolicySearchCriteria` domain object.

---

## 3. Technology Stack

| Concern         | Technology                                   |
| --------------- | -------------------------------------------- |
| Language        | Java 21 (LTS)                                |
| Framework       | Spring Boot 3.4.5 (Web, Data JPA, Validation, Actuator) |
| Database        | PostgreSQL                                   |
| Migrations      | Flyway                                       |
| Build           | Maven (wrapper committed: `mvnw` / `mvnw.cmd`) |
| API docs        | springdoc-openapi (Swagger UI)               |
| Boilerplate     | Lombok                                       |
| Testing         | JUnit 5, Mockito, Testcontainers             |
| Containerisation| Docker Compose                               |

---

## 4. Prerequisites

- **JDK 21 or newer** on the `PATH` (the build targets Java 21).
- **Docker Desktop** (for PostgreSQL via Docker Compose and for Testcontainers-based
  integration tests).
- No global Maven needed — use the bundled wrapper (`./mvnw`, or `mvnw.cmd` on Windows).

---

## 5. Local Setup

```bash
# 1. Clone
git clone <repo-url>
cd policy-overview-bff

# 2. Start PostgreSQL (see Docker Setup below)
docker compose up -d

# 3. Build and run
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080**. On first start, Flyway creates the
schema and a data seeder inserts 250 sample APAC policies (only if the table is empty).

---

## 6. PostgreSQL Setup

The application connects using the settings in
[`src/main/resources/application.yml`](src/main/resources/application.yml):

| Property | Value                                        |
| -------- | -------------------------------------------- |
| URL      | `jdbc:postgresql://localhost:5432/policydb`  |
| Username | `postgres`                                   |
| Password | `postgres`                                   |
| Database | `policydb`                                   |

The easiest way to provision this is the bundled Docker Compose file (next section). If
you prefer a locally installed PostgreSQL, create a database named `policydb` reachable
with the credentials above.

> `spring.jpa.hibernate.ddl-auto` is set to **`validate`** — Hibernate never creates or
> alters tables; the schema is owned exclusively by Flyway.

---

## 7. Docker Setup

A single-service [`docker-compose.yml`](docker-compose.yml) provisions PostgreSQL with a
persistent volume and a readiness healthcheck.

```bash
# Start PostgreSQL in the background
docker compose up -d

# Check health / status
docker compose ps

# Tail logs
docker compose logs -f postgres

# Stop (data is preserved in the named volume)
docker compose down

# Stop and delete the data volume (full reset)
docker compose down -v
```

| Setting        | Value             |
| -------------- | ----------------- |
| Image          | `postgres:18`     |
| Container name | `policydb`        |
| Port           | `5432:5432`       |
| Volume         | `policydb-data`   |

---

## 8. Running the Application

```bash
# Run with the Spring Boot plugin (hot-friendly for local dev)
./mvnw spring-boot:run

# Or build a jar and run it
./mvnw clean package
java -jar target/policy-overview-bff-0.0.1-SNAPSHOT.jar
```

Once running:

- **API base path:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health:** http://localhost:8080/actuator/health

The hand-authored API contract also lives at
[`src/main/resources/openapi/policies-api.yaml`](src/main/resources/openapi/policies-api.yaml).

---

## 9. Flyway Migrations

Migrations live in
[`src/main/resources/db/migration`](src/main/resources/db/migration) and run automatically
on application startup.

- `V1__init_policy_schema.sql` — creates the `policy` table (UUID primary key, unique
  `policy_number`, and indexes on `status`, `line_of_business`, `region`, `effective_date`).

Naming convention: `V<version>__<description>.sql`. To add a change, create the next
versioned file (e.g. `V2__add_xyz.sql`) — never edit an already-applied migration.

---

## 10. API Endpoints

Base path: `/api/v1`

| Method  | Path                       | Description                                                |
| ------- | -------------------------- | ---------------------------------------------------------- |
| `GET`   | `/policies`                | Paginated, filtered, sorted, searchable list of policies.  |
| `GET`   | `/policies/{id}`           | Single policy detail by UUID.                              |
| `PATCH` | `/policies/flag`           | Flag one or more policies for review.                      |
| `GET`   | `/policies/summary`        | Aggregate counts and premium totals.                       |

### `GET /policies` query parameters

| Param               | Type    | Notes                                            |
| ------------------- | ------- | ------------------------------------------------ |
| `page`              | int     | Zero-based page index (default `0`).             |
| `size`              | int     | Page size (default `20`).                        |
| `sort`              | string  | e.g. `premiumAmount,desc`.                       |
| `status`            | enum    | `Active`, `Expired`, `Pending`, `Cancelled`.     |
| `lineOfBusiness`    | enum    | `Property`, `Casualty`, `A&H`, `Marine`.         |
| `region`            | enum    | `Singapore`, `Hong Kong`, `Australia`, `Japan`, `Thailand`, `Indonesia`, `Malaysia`, `Philippines`. |
| `effectiveDateFrom` | date    | ISO `yyyy-MM-dd`, inclusive lower bound.         |
| `effectiveDateTo`   | date    | ISO `yyyy-MM-dd`, inclusive upper bound.         |
| `search`            | string  | Case-insensitive across policy number, holder name, underwriter. |

> Enum filters accept their **display values** (e.g. `status=Active`, `lineOfBusiness=A&H`).

### Examples

```bash
# Page 0, 10 per page, highest premium first, active policies in Singapore
curl "http://localhost:8080/api/v1/policies?page=0&size=10&sort=premiumAmount,desc&status=Active&region=Singapore"

# Single policy
curl "http://localhost:8080/api/v1/policies/3fa85f64-5717-4562-b3fc-2c963f66afa6"

# Flag policies for review
curl -X PATCH "http://localhost:8080/api/v1/policies/flag" \
  -H "Content-Type: application/json" \
  -d '{"policyIds":["3fa85f64-5717-4562-b3fc-2c963f66afa6"]}'

# Summary
curl "http://localhost:8080/api/v1/policies/summary"
```

---

## 11. Running Tests

```bash
# Run the full suite (unit + integration). Integration tests require Docker.
./mvnw test

# Unit tests only (no Docker needed)
./mvnw test -Dtest='PolicyServiceImplTest,DomainToResponseDtoTest,EntityToDomainTest,PolicySpecificationTest'
```

- **Unit tests** — JUnit 5 + Mockito for the service, mappers, and specification builders.
- **Integration tests** — `*IntegrationTest` use **Testcontainers** to spin up a real
  PostgreSQL (`@ServiceConnection`), so **Docker must be running**.

---

## 12. Project Structure

```
src/main/java/com/chubb/policyoverview
├── api
│   ├── controller        # REST controllers (thin; delegate to service)
│   ├── dto
│   │   ├── request       # inbound request DTOs
│   │   └── response      # outbound response DTOs
│   ├── mapper            # domain → response DTO mapping
│   └── exception         # (reserved for @RestControllerAdvice)
├── domain
│   ├── models            # pure business models, enums, search criteria
│   └── exception         # domain-specific exceptions
├── service               # business logic / use cases
├── infrastructure
│   └── persistence
│       ├── entity        # JPA entities
│       ├── repository    # Spring Data repositories
│       ├── specification # JPA Specifications (dynamic filtering)
│       └── mapper        # entity ↔ domain mapping
├── config                # Spring configuration (web conversion, data seeder)
└── common                # cross-cutting concerns (util, logging, exception)

src/main/resources
├── application.yml
├── db/migration          # Flyway migrations
└── openapi               # hand-authored OpenAPI contract
```

---

## 13. Design Decisions

- **Clean, layered architecture** — the `domain` core has no framework dependencies;
  DTOs stay in `api`, entities stay in `infrastructure`, and dedicated mappers translate
  at each boundary. This keeps business logic isolated and testable.
- **Dynamic filtering via JPA Specifications** — `PolicySearchCriteria` (a framework-free
  builder-based domain object) is composed into a `Specification` null-safely; absent
  filters become no-op predicates, so any combination of filters works without branching.
- **Display vs. stored values** — enums are stored by constant name in the database and
  surfaced to clients as human-readable display values (`ACTIVE` → `Active`,
  `ACCIDENT_AND_HEALTH` → `A&H`). A `WebMvcConfigurer` registers converters so query-param
  filters accept the display values.
- **Flyway owns the schema** — `ddl-auto=validate` guarantees the entity mapping and the
  migrated schema stay in lock-step; drift fails fast at startup.
- **Constructor injection only** — no field injection, which keeps components immutable
  and trivially unit-testable with plain Mockito.
- **Contract-first** — the OpenAPI document was authored up front and the controllers/DTOs
  follow it.
- **Seeded sample data** — a `CommandLineRunner` inserts 250 realistic APAC policies on an
  empty database for an out-of-the-box demo experience.

> **Build note:** the build/test JVM in some environments is newer than Java 21. Lombok
> and Byte Buddy versions are overridden above the Spring Boot BOM in `pom.xml` to support
> newer JDKs; on a Java 21 toolchain the BOM defaults also work.

---

## 14. Future Enhancements

- **Global exception handling** — a `@RestControllerAdvice` mapping domain/validation
  exceptions to the contract's `ErrorResponse` (404/400/503), replacing default error JSON.
- **Authentication & authorization** — currently handled by the platform team; wire in
  once available.
- **Additional filtering & export** — status/region facets and CSV export are out of scope
  for the current ticket and planned separately.
- **Correlation IDs & structured logging** — request-scoped correlation IDs and access
  logging in `common/logging`.
- **Caching** — cache summary/aggregate queries for high-read, low-write workloads.
- **Containerise the application** — add a multi-stage Dockerfile and a `bff` service in
  Docker Compose (`depends_on` the healthy database) for a one-command full stack.
- **CI/CD pipeline** — automated build, test (with Testcontainers), and image publishing.
```
