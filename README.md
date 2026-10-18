# Restful-Booker API Test Automation + CI/CD

![CI](https://github.com/fahadbadri05-Qa/RST_ASSURD_DevOps/actions/workflows/ci.yml/badge.svg)

REST Assured test framework for the public [Restful-Booker API](https://restful-booker.herokuapp.com/apidoc/index.html) (auth plus full CRUD), with a GitHub Actions pipeline, Allure reporting, parallel execution, data-driven tests, WireMock stubs and a Gatling load test.

**Live reports, published from CI on every push to `main`:**

- Functional test report (Allure): https://fahadbadri05-qa.github.io/RST_ASSURD_DevOps/
- Load test report (Gatling): https://fahadbadri05-qa.github.io/RST_ASSURD_DevOps/load-test/

## What is in here

| Area | Details |
|---|---|
| Functional API tests | 18 tests (JUnit 5 + REST Assured 5): auth, create, read, update, delete, JSON schema validation |
| Suites | `@Tag("smoke")` (5 tests) and `@Tag("regression")` (13 tests including parameterized cases), selected with `-Dgroups=` |
| Data-driven tests | `@ParameterizedTest` with `@ValueSource` and `@CsvSource` (7 cases from 2 methods) |
| Parallel execution | JUnit 5 concurrent classes and methods via `junit-platform.properties` |
| Mocking | WireMock in-JVM test (port 8089); standalone stubs (port 8081) used by the load test and by `mvn test -Denv=mock` (16 functional tests; the delete lifecycle test is skipped because stateless stubs cannot model it) |
| Load test | Gatling (Java DSL): 20 users ramped over 10 s, 4 requests each, with pass/fail assertions |
| Reporting | Allure, published to GitHub Pages |
| CI/CD | GitHub Actions; a declarative Jenkinsfile (any agent with Java and Maven) is also provided |
| Packaging | Dockerfile; CI publishes the image to GitHub Container Registry |

## CI/CD pipeline (GitHub Actions)

| Trigger | What runs |
|---|---|
| push or pull request to `main` | smoke suite |
| nightly at 02:00 UTC | full suite |
| manual (`workflow_dispatch`) | smoke, regression or all, selectable |

Every run then starts a standalone WireMock, runs the Gatling load test (the build fails if any request fails or the max response time exceeds 1000 ms), generates the Allure report, adds the Gatling report under `/load-test/`, and publishes both to GitHub Pages (main branch only).

## Load test

- Scenario: auth -> create booking -> get booking by id -> get all bookings.
- Load: 20 virtual users ramped over 10 seconds (80 requests in total).
- Assertions: zero failed requests, and max response time under 1000 ms.
- Target: local WireMock stubs, **not** the live Restful-Booker service. The live site is a shared free demo server and should not be load tested. The results therefore validate the load-test setup and the pipeline, not the performance of the real API.
- Example CI run: 80/80 requests OK, p95 6 ms, p99 17 ms, max 46 ms.

## Run it locally

```bash
# all functional tests
mvn test

# only smoke / only regression
mvn test -Dgroups=smoke
mvn test -Dgroups=regression

# against another environment (loads config-<env>.properties)
mvn test -Denv=staging
mvn test -Denv=mock   # local WireMock stubs on port 8081 (start WireMock first, see the load test steps below)

# view the Allure report
mvn allure:serve
```

Load test (needs Java 17 and Maven). Terminal 1 starts WireMock:

```bash
curl -L -o wiremock-standalone.jar https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.13.1/wiremock-standalone-3.13.1.jar
java -jar wiremock-standalone.jar --port 8081 --root-dir wiremock
```

On Windows PowerShell, download with `Invoke-WebRequest -Uri <same url> -OutFile wiremock-standalone.jar`.

Terminal 2 runs the test and prints the report location when done:

```bash
mvn gatling:test
```

## Project structure

```
src/main/java/com/apitest/
  config/ConfigManager.java       env resolution: system property -> env var -> properties file
  models/                         Booking, BookingDates, AuthRequest, AuthResponse (POJOs)
  clients/                        AuthClient, BookingClient (REST Assured request layer)

src/test/java/com/apitest/tests/
  BaseTest.java                   shared setup, request/response logging, Allure filter
  AuthTests.java                  token generation
  GetBookingTests.java            read + JSON schema validation
  CreateBookingTests.java         booking creation
  UpdateBookingTests.java         update + missing-token case
  DeleteBookingTests.java         create -> delete -> verify gone
  DataDrivenBookingTests.java     parameterized boundary IDs and booking payloads
  WireMockBookingTests.java       in-JVM WireMock test

src/test/java/simulations/
  BookingLoadSimulation.java      Gatling load test

src/test/resources/
  config.properties               default environment values
  junit-platform.properties       parallel execution settings
  schemas/booking-schema.json     JSON schema for response validation

wiremock/mappings/                stubs for auth, create booking, get all, get by id
.github/workflows/ci.yml          GitHub Actions pipeline
Jenkinsfile                       declarative pipeline with ENVIRONMENT and TEST_SUITE parameters
Dockerfile                        container for running the suite
k8s/test-cronjob.yml              CronJob that runs the regression suite from the published image
k8s/kyverno-policy.yml            ClusterPolicy: require a Cosign signature from this workflow
```

## Docker and Kubernetes

- **Image:** every push to `main` builds the image in CI and publishes it to GitHub Container Registry as `ghcr.io/fahadbadri05-qa/rst_assurd_devops`, tagged `latest` and with the commit SHA. The job only runs after the API tests pass.
- **Run it:** the image entrypoint is `mvn test`, so arguments are Maven options: `docker run --rm ghcr.io/fahadbadri05-qa/rst_assurd_devops:latest -Dgroups=smoke`
- **CronJob:** `k8s/test-cronjob.yml` runs the regression suite at 02:00 UTC in the `qa-automation` namespace, with `restartPolicy: Never`, no retries (a failing test is a result, not a transient error), a 30 minute deadline and resource limits.
- **Verified:** a Job created from the CronJob ran the 13 regression tests to BUILD SUCCESS on Docker Desktop Kubernetes, using the image published by CI.
- **Scheduled runs:** the cluster created Jobs on the 02:00 UTC schedule without intervention. Of the four scheduled Jobs still listed, three completed in under a minute each. The fourth started about 13 hours after its slot (apparently a catch-up run after the machine had been off), ended in Error, and its logs were already gone, so the cause is unknown.
- **Limitation:** the cluster is Docker Desktop on a laptop, so it only runs schedules while the machine and Docker Desktop are on. It complements the nightly GitHub Actions run and does not replace it. Any cluster other than Docker Desktop is untested.
- **Build note:** the workflow sets up Docker Buildx before building. Without it the image was published as a single manifest, and on Docker Desktop (containerd image store) Kubernetes failed to read it with `ImageInspectError`. Adding Buildx fixed it; the underlying cause was not established.

```bash
kubectl create namespace qa-automation
kubectl apply -f k8s/test-cronjob.yml
kubectl create job --from=cronjob/restful-booker-regression-cron manual-1 -n qa-automation
kubectl logs job/manual-1 -n qa-automation
```

## Design decisions

- **Layered configuration.** `ConfigManager` resolves values from the `-Denv=` system property, then environment variables (for example `API_BASE_URL`, so CI can inject values without committing them), then properties files.
- **Tags instead of separate suites.** Smoke tests are a tagged subset of the same classes, filtered with `-Dgroups=`, so nothing is duplicated.
- **Request layer separated from tests.** Tests call `BookingClient` and `AuthClient` and work with POJOs, so an API change is fixed in one place.
- **Schema validation.** Responses are checked against a JSON schema, not just individual fields.
- **Load test targets stubs.** A shared public demo API should not receive load, and a local stub gives repeatable numbers.
- **CI cadence.** Fast smoke feedback on every push, full regression nightly.

## Measured result: parallel execution

Enabling concurrent classes and methods reduced a full run from 30.05 s to 28.2 s (about 6 percent). The gain is small because the suite is network-bound (calls to an external API) and a fixed JVM and Maven startup cost is unaffected by parallelism.

## Roadmap (not done yet)

- **Kubernetes:** the image is referenced as `:latest`; pin the commit SHA tag for reproducible runs, and keep Job logs somewhere persistent so a failed run can be diagnosed.
- **Mock profile in CI:** `-Denv=mock` runs locally but is not part of the pipeline yet. It needs its own Allure results directory so live and mock runs do not mix, and the delete lifecycle could be modelled with WireMock scenarios.
- **Jenkins:** add a Gatling stage to the Jenkinsfile; the Jenkinsfile has not been verified against a live Jenkins yet.

## Supply chain security (Sigstore and Kyverno)
- **Keyless Container Signing (Sigstore/Cosign):** Automatically signs test container images post-build in GitHub Actions using ephemeral certificates and GitHub's OIDC issuer (id-token: write).
- **Admission Control Policy Enforcement (Kyverno):** `k8s/kyverno-policy.yml` is a ClusterPolicy in Enforce mode that requires images matching `ghcr.io/fahadbadri05-qa/rst_assurd_devops*` to carry a keyless Cosign signature from this repository workflow (`ci.yml` on `main`). It was applied to the Docker Desktop cluster and reported Ready. Blocking of an unsigned image has not been demonstrated yet.
- **Limitation:** verification is expected to fail closed. If the registry or the Rekor log is unreachable, or Kyverno is down, the CronJob cannot start.
