# AI Journal

A chronological record of AI-assisted changes to the Policy Overview BFF, capturing what
was done and the key decisions/trade-offs behind each step.

---

## 1. Project scaffolding (Spring Boot + Maven, Java 21)

- Converted the repository into a Spring Boot **3.4.5** / Java **21** Maven project.
- Obtained a correct Maven Wrapper (Maven 3.9.16, script-only) via Spring Initializr, then
  hand-wrote a `pom.xml` pinned to the 3.4.x line (Initializr only serves >= 3.5.0).
- Created the layered package skeleton under `com.chubb.policyoverview`, `application.yml`,
  a Flyway baseline migration, and `.gitignore`.
- **Decision:** pinned Spring Boot 3.4.5 per `tech-stack.md` rather than the newer 3.5.x.

## 2. Package rename

- Renamed the base package `com.chubb.assessment` → `com.chubb.policyoverview` across
  main/test sources, README, and the architecture rule doc.

## 3. Datasource externalization (first pass)

- Pointed `spring.datasource.*` at environment variables.

## 4. JPA entity `Policy`

- Added an example JPA entity mapped to the `policy` table with a UUID primary key.
- Field naming iterated per request: `customerName` → `policyholderName`; the DB column
  `holder_name` → `policyholder_name` (migration + entity kept in lock-step).

## 5. OpenAPI contract

- Authored `src/main/resources/openapi/policies-api.yaml` (OpenAPI 3.0.3): base path
  `/api/v1`, four endpoints, reusable schemas, enums, validation, examples, error model.
- Renamed the list `operationId` `listPolicies` → `getPolicies`.

## 6. Schema/model alignment to the contract

- Expanded the `policy` migration, JPA entity, domain model, and DTOs to the full 14-field
  `Policy` schema (UUID id, `Status`/`LineOfBusiness`/`Region` enums, premium, dates,
  underwriter, `flaggedForReview`, timestamps).
- Added named PK/unique constraints and filtering indexes
  (`status`, `line_of_business`, `region`, `effective_date`).
- **Decision:** enums stored by constant name in the DB; display values (e.g. `A&H`,
  `Hong Kong`) surfaced via `getDisplayName()`.

## 7. Persistence layer + data seeder

- Renamed the entity to `PolicyEntity`; added an insert constructor (no boolean param —
  flagging via `flagForReview()`).
- Added `PolicyRepository` (`JpaRepository` + `JpaSpecificationExecutor`).
- Added `PolicyDataSeeder` (`CommandLineRunner`, 250 realistic APAC policies when empty)
  and split realistic-data generation into `PolicySampleDataFactory` (SRP + size limits;
  `EnumMap` instead of switches to keep complexity < 4).
- Removed the placeholder controller/service to be rewritten later.

## 8. Dynamic filtering with JPA Specifications

- Added `PolicySearchCriteria` (framework-free, builder, `Optional` getters) in `domain`.
- Added `PolicySpecification` in `infrastructure` with reusable, null-safe predicate
  builders (absent filters → no-op `conjunction()`); case-insensitive free-text search
  across `policyNumber`, `policyholderName`, `underwriter`.
- Added a Testcontainers integration test demonstrating repository + specification usage.

## 9. Service layer

- Added `PolicyService` interface + `PolicyServiceImpl` (constructor injection):
  `getPolicies`, `getPolicyById` (throws `PolicyNotFoundException`), `flagPolicies`,
  `getPolicySummary` (DB-side aggregation, all statuses/LOBs pre-seeded to zero).
- Added `EntityToDomain` mapper so flow is `Entity → domain → ResponseDto`.
- **Decision/trade-off:** per the explicit request the service returns api DTOs, which
  deviates from the strict "services operate on domain models only" rule; flagged the
  compliant alternative.

## 10. DTOs from the contract (Lombok)

- Generated `PolicyResponseDto`, `PolicyDetailResponseDto`, `PolicySummaryResponseDto`,
  `FlagPoliciesRequestDto` (with validation), `FlagPoliciesResponseDto`, `ErrorResponseDto`.
- Adjusted the service + mapper to use them; removed the superseded placeholder records.
- **Build fix:** JDK 23+ disables implicit annotation processing — configured
  `maven-compiler-plugin` with an explicit Lombok `annotationProcessorPaths` + `proc=full`,
  and overrode `lombok.version` to 1.18.46 for JDK 25.
- Added `@AllArgsConstructor` to the response DTOs on request.

## 11. REST controllers

- Added `PolicyController` (`/api/v1/policies`, `/{id}`, `/flag`, `/summary`); thin,
  constructor injection, `@Valid` body, `Pageable` for pagination/sorting.
- Added `PagedResponseDto<T>` to match the contract's paged shape (Spring `Page` doesn't).
- Added `WebConversionConfig` registering display-name → enum converters so contract
  filter values (`status=Active`, `lineOfBusiness=A&H`) bind correctly.

## 12. Swagger UI

- Added `springdoc-openapi-starter-webmvc-ui` 2.8.17 (serves `/swagger-ui.html`).

## 13. Unit tests (JUnit 5 + Mockito)

- Added tests for `PolicyServiceImpl`, the two mappers, and `PolicySpecification`
  (Criteria API mocked). AAA pattern, happy + failure paths, repository mocked.
- **Build fix:** overrode `byte-buddy.version` to 1.18.10 so Mockito can mock concrete
  classes on JDK 25. All 19 unit tests pass.

## 14. Docker Compose

- Added `docker-compose.yml` with a single `postgres:18` service (`policydb`), persistent
  named volume, and a `pg_isready` healthcheck.

## 15. README

- Authored a professional `README.md` (overview, architecture, stack, setup, Docker,
  Flyway, endpoints, tests, structure, design decisions, future enhancements).

## 16. Database config externalization + `.env`

- `application.yml` datasource now uses `${DB_URL:...}` / `${DB_USERNAME:...}` /
  `${DB_PASSWORD:...}` with local defaults (backward compatible).
- `docker-compose.yml` parameterized via `${VAR:-default}` interpolation.
- Added `.env` (git-ignored) + `.env.example` (committed template); updated `.gitignore`.

## 17. Global exception handling (`@RestControllerAdvice`)

- Added `GlobalExceptionHandler` in `api/exception` per the architecture rule that
  exception→HTTP translation is an `api` concern; reuses the existing `ErrorResponseDto`
  and matches the OpenAPI `ErrorResponse` contract (timestamp/status/error/message/path).
- Mapped exceptions: `PolicyNotFoundException` → **404**; `MethodArgumentNotValidException`,
  `ConstraintViolationException`, `MethodArgumentTypeMismatchException`,
  `HttpMessageNotReadableException` → **400**; catch-all `Exception` → **500**.
- **Logging:** SLF4J parameterized messages including method + path + correlation ID
  (read from MDC, falls back to `N/A`) + status; `WARN` for 4xx, `ERROR` with stack trace
  for 5xx. No sensitive data (policy number/amount) logged.
- **Code style:** error reason phrases via `HttpStatus.getReasonPhrase()` (no magic
  strings), each handler kept under the complexity-< 4 limit (only branch is the 4xx/5xx
  split in `logFailure`), file under 200 lines.
- Added `GlobalExceptionHandlerTest` (6 cases, `HttpServletRequest` mocked) — all pass.
- **Decision/trade-off:** correlation ID is read from `MDC` under key `correlationId`, but
  no MDC-populating filter exists yet (`common/logging` is empty), so it currently logs
  `N/A`; a correlation-ID servlet filter is the natural follow-up.

---

## Outstanding / known follow-ups

- **Correlation-ID filter** (`common/logging`) to populate `MDC` so the exception handler
  logs a real request ID instead of `N/A`.
- **503 Service Unavailable** mapping (defined in the OpenAPI contract) — not yet wired;
  no downstream dependency currently throws an exception to map.
- **JDK note:** `lombok.version` and `byte-buddy.version` are overridden above the Spring
  Boot BOM to support newer JDKs (build/test JVM is JDK 25); BOM defaults suffice on JDK 21.
- **PostgreSQL version drift:** Docker Compose uses `postgres:18` while `tech-stack.md`
  states 16.x — reconcile.
- Controller/web-layer tests (`@WebMvcTest`) not yet added.
