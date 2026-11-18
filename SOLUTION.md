# Time Deposit Kata solution

## Run with Docker Compose

From the repository root, run `make run` with Docker Compose available. It builds the Java 25 image, waits for PostgreSQL 17 to accept connections, and starts the application in the background. Use `docker compose stop` to stop both services while keeping the local database volume.

Compose enables the `demo` profile. Spring loads [demo-data.sql](java/src/main/resources/demo-data.sql) after Hibernate creates the tables. An empty database gets one deposit per plan and three withdrawals. Existing deposits and withdrawals are preserved on restart.

Run `make stop` to remove the Compose containers and delete the local PostgreSQL data volume.

## Exercise the API

After `make run`, open [Swagger UI](http://localhost:8080/swagger-ui.html). Compose binds the app to loopback on port `8080`. Set `APP_PORT` to use another host port. The demo profile exposes the generated [OpenAPI YAML](http://localhost:8080/v3/api-docs.yaml).

| Operation | Result |
| --- | --- |
| `GET /time-deposits` | Returns the full JSON array of deposits with their withdrawals, without pagination. |
| `POST /time-deposits/accruals` | Credits one month of interest and returns `204` after the transaction commits. Each successful call applies another accrual. |

```sh
curl -fsS http://localhost:8080/time-deposits
curl -fsS -X POST http://localhost:8080/time-deposits/accruals
```

## Run the tests

Use JDK 25 and a running Docker daemon. The project uses Spring Boot 4.1.2. The Maven Wrapper downloads Maven 3.9.16 on the first run, and Testcontainers starts a disposable PostgreSQL 17 instance for the persistence test.

```sh
make test
```

## Initialize PostgreSQL

With a local PostgreSQL database available, set `SPRING_DATASOURCE_URL` to its JDBC URL and set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. From `java/`, run `./mvnw spring-boot:run`.

Hibernate creates `time_deposits` and `withdrawals` from the Spring Data JPA entities on startup. PostgreSQL stores balances and withdrawal amounts as `numeric` so the database preserves their decimal precision. Withdrawals have a required deposit reference and an index on `(time_deposit_id, id)`.

## Folder structure

The repository root contains the Compose file and Make targets. The Java application, Dockerfile, Maven build, and wrapper live in `java/`. Application packages are under `java/src/main/java/org/ikigaidigital/`.

| Package | Responsibility |
| --- | --- |
| `api/` | HTTP controller and the annotated `TimeDepositApi` interface that defines the OpenAPI contract. |
| `dto/` | Deposit and withdrawal response records returned by GET. |
| `service/` | `TimeDepositService` maps the listing to response records and coordinates transactional accrual in batches of 1,000 deposits. |
| `domain/` | The shared `TimeDeposit` model used by the calculator. |
| `calculator/` and `calculator/plan/` | Framework-free balance calculation and the interest strategy for each plan. |
| `configuration/` | Spring bean definitions that wire the plan strategies into the calculator. |
| `persistence/` | JPA entities and Spring Data repositories for deposits and withdrawals. |

`java/src/main/resources/` contains application settings and the demo SQL seed. `java/src/test/java/` contains calculator and wiring tests, plus API and persistence tests backed by PostgreSQL Testcontainers.

## Architecture decisions

- [0001 — Full-portfolio processing with Spring Data](docs/adr/0001-full-portfolio-processing.md)
- [0002 — Preserve legacy balance arithmetic](docs/adr/0002-legacy-balance-arithmetic.md)
- [0003 — Keep plan strategies independent of Spring](docs/adr/0003-plan-interest-strategies.md)
- [0004 — Define the HTTP contract at the controller boundary](docs/adr/0004-http-contract.md)

## Business assumptions

- **Starting data.** Deposits and withdrawals are prefilled. The demo profile seeds an empty database. The API lists existing records and updates existing balances.
- **Listing size.** GET returns all deposits and withdrawals in one response. Pagination is omitted for simplicity, so memory use grows with the full result.
- **Plan names.** The calculator matches `basic`, `student`, and `premium` exactly, including case and whitespace. An unregistered value such as `Basic` raises `InvalidPlanTypeException`. The calculator validates the entire input list before updating its balances.
- **Eligibility.** All three plans earn zero interest through day 30. Each strategy owns its cutoff because waiting periods may change independently. Basic starts on day 31, Student earns interest from day 31 through day 365 inclusive, and Premium starts on day 46.
- **Repeated accrual.** Each successful POST credits one month of interest using the current balance and stored `days`. The day count stays fixed. Retrying a successful request credits another month.
- **Balance precision.** The legacy calculator rounds interest to cents and adds it to the balance using `double` arithmetic. PostgreSQL stores the decimal representation of the result when the balance changes. For example, a Basic deposit of `18.000000000000001` on day 31 becomes `18.01`. A deposit that receives no credit keeps its original stored precision.

## AI-assisted development

Codex used the **Superpowers** plugin for test-driven refactoring, coordinating parallel agent work, and verification. The **Ponytail** plugin guided simplification during review. For example, the final service reuses the Spring Data repository for both endpoints and calls the existing calculator once per accrual batch.

Codex helped write the calculator characterization tests, the plan strategies, the PostgreSQL integration tests, and the HTTP API. Separate agents worked on Swagger and DTOs, deposit listing, and atomic batch accrual. Use `make test` to reproduce the combined test run. The repository records the workflow and compatibility constraints in [AGENTS.md](AGENTS.md).
