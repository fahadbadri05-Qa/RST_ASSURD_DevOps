# Restful-Booker API Test Automation + CI/CD

A REST Assured framework testing the [Restful-Booker API](https://restful-booker.herokuapp.com/apidoc/index.html) — a public API purpose-built for testing practice (auth, full CRUD, realistic edge cases). Wired into both **Jenkins** (matching a typical enterprise Docker-agent setup) and **GitHub Actions** (for public CI on the repo itself), with Allure reporting, environment switching, and smoke/regression tagging.

## Live report

Latest Allure report, auto-published on every push to `main`: https://fahadbadri05-qa.github.io/RST_ASSURD_DevOps/

## Stack

- Java 17, Maven
- REST Assured 5 + JSON Schema Validator
- JUnit 5 (with `@Tag` for smoke/regression suites)
- Allure Report
- Docker
- Jenkins (declarative, Docker agent) + GitHub Actions

## Project structure

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

## Completed milestones

1. **Parallel execution** — Enabled via `junit-platform.properties` (concurrent classes + methods). Reduced total run time from 30.05s to 28.2s (~6%). The gain is modest because the suite is network-bound (calls to a live external API) rather than CPU-bound, and a fixed JVM/Maven startup cost is unaffected by parallelism — a deliberate, measured result rather than a big round number.
2. **Data-driven tests** — `DataDrivenBookingTests` uses `@ParameterizedTest` with `@ValueSource` (boundary invalid IDs: 0, -1, 999999999) and `@CsvSource` (varied booking payloads). Grew the suite from 10 to 17 tests without duplicating assertion logic.
3. **Allure → GitHub Pages** — `peaceiris/actions-gh-pages` step in `ci.yml` publishes the report to a `gh-pages` branch on every push to `main`, served live via GitHub Pages (link above).

## Next milestones (stretch goals to extend this further)

1. **WireMock mock layer** — stand up a Dockerized WireMock instance so the suite doesn't depend on the (sometimes slow/flaky) public Heroku instance; run contract-style tests against both.
2. **Kubernetes CronJob** — replace/complement the Jenkins cron trigger with a K8s CronJob running the Docker image, since you've already done K8s-based parallel execution professionally.
3. **Slack/Teams webhook on failure** — wire the `failure` post-block (Jenkinsfile) or a GitHub Actions step to notify on regression breaks.
