# Restful-Booker API Test Automation + CI/CD

A REST Assured framework testing the [Restful-Booker API](https://restful-booker.herokuapp.com/apidoc/index.html) — a public API purpose-built for testing practice (auth, full CRUD, realistic edge cases). Wired into both **Jenkins** (matching a typical enterprise Docker-agent setup) and **GitHub Actions** (for public CI on the repo itself), with Allure reporting, environment switching, and smoke/regression tagging.

## Stack

- Java 17, Maven
- REST Assured 5 + JSON Schema Validator
- JUnit 5 (with `@Tag` for smoke/regression suites)
- Allure Report
- Docker
- Jenkins (declarative, Docker agent) + GitHub Actions

## Project structure

```
src/main/java/com/apitest/
  config/ConfigManager.java     -> env resolution: system property -> env var -> properties file
  models/                       -> Booking, BookingDates, AuthRequest, AuthResponse (POJOs)
  clients/                      -> AuthClient, BookingClient (REST Assured request layer)

src/test/java/com/apitest/tests/
  BaseTest.java                 -> shared setup, request/response logging, Allure filter
  AuthTests.java                -> token generation (smoke)
  GetBookingTests.java          -> read + JSON schema validation
  CreateBookingTests.java       -> booking creation
  UpdateBookingTests.java       -> update + auth-negative case
  DeleteBookingTests.java       -> full create -> delete -> verify-gone lifecycle

src/test/resources/
  config.properties             -> default (qa) environment values
  schemas/booking-schema.json   -> JSON schema for response validation

Jenkinsfile                     -> declarative pipeline, env + suite as build parameters
.github/workflows/ci.yml        -> smoke on every push/PR, full regression nightly at 2 AM UTC
Dockerfile                      -> containerized test run
```

## Run it locally

```bash
# everything
mvn test

# just smoke tests
mvn test -Dgroups=smoke

# just regression
mvn test -Dgroups=regression

# against a different environment (add config-staging.properties etc. to extend)
mvn test -Denv=staging

# view the report
mvn allure:serve
```

## Run it in Docker

```bash
docker build -t booker-api-tests .
docker run --rm booker-api-tests
```

## Run it in Jenkins

Point a Pipeline job at this repo (`Jenkinsfile` is auto-detected). It exposes `ENVIRONMENT` and `TEST_SUITE` as build parameters, so you can trigger `staging` + `regression` from the Jenkins UI without touching code. Needs the Allure Jenkins plugin installed to render the `post { always { allure ... } }` step; drop that block if you don't have it configured.

## Design choices worth knowing for an interview

- **Env resolution order** (`ConfigManager`): system property `-Denv=` → env var override (e.g. `API_BASE_URL`, useful for injecting secrets in CI without committing them) → properties file. This is the same layered pattern you'd use to avoid hardcoding URLs/creds across qa/staging/prod.
- **Tag-based suites, not separate test classes**: `@Tag("smoke")` / `@Tag("regression")` on individual `@Test` methods, filtered via `-Dgroups=`. Keeps smoke tests as a true subset of regression instead of duplicated code — mirrors the smoke/regression split from your ServiceNow ATF work.
- **POJO request/response mapping** instead of raw JSON strings or `jsonPath()` everywhere — `AuthClient` deserializes straight into `AuthResponse`, which is the idiomatic REST Assured pattern and scales better as the API surface grows.
- **CI cadence**: smoke on every push (fast feedback), full regression nightly via cron — the same shape as a real release pipeline, not "run everything every time."

## Next milestones (stretch goals to extend this further)

1. **Parallel execution** — JUnit 5 `junit-platform.properties` with `junit.jupiter.execution.parallel.enabled=true`, then measure the before/after runtime (you already have a great "reduced X to Y" story from LETITBEX — build the same evidence here).
2. **WireMock mock layer** — stand up a Dockerized WireMock instance so the suite doesn't depend on the (sometimes slow/flaky) public Heroku instance; run contract-style tests against both.
3. **Kubernetes CronJob** — replace/complement the Jenkins cron trigger with a K8s CronJob running the Docker image, since you've already done K8s-based parallel execution professionally.
4. **Publish Allure to GitHub Pages** — so the report is a live link you can drop straight into your portfolio/resume, not just a downloadable artifact.
5. **Slack/Teams webhook on failure** — wire the `failure` post-block (Jenkinsfile) or a GitHub Actions step to notify on regression breaks.
6. **Data-driven tests** — `@ParameterizedTest` + CSV/JSON source for booking payloads (boundary prices, missing fields, invalid date formats) to bulk out negative-path coverage.
