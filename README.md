# API Testing Lab — REST Assured

Automated CRUD test suite for the [JSONPlaceholder](https://jsonplaceholder.typicode.com)
REST API, built as a lab project for a QA Specialization training module (API Testing
with REST Assured). Covers full CRUD coverage, JSON schema validation, Allure
reporting, Docker containerization, and a GitHub Actions CI/CD pipeline.

## Tech stack

- Java 21, Maven
- REST Assured 5.x + JSON Schema Validator
- JUnit 5 (Jupiter), Hamcrest
- Jackson Databind (POJO mapping)
- Allure (JUnit5 + REST Assured integration) for reporting
- Docker
- GitHub Actions

## Project structure

```
src/test/java/org/automation/
  base/        shared test configuration (base URI, request spec)
  models/      POJOs for request/response mapping
  tests/       test classes per resource (posts, comments, users)
  utils/       test helpers
src/test/resources/schemas/   JSON schema files used for response validation
docs/                         test summary / reporting docs
```

## Important constraint

JSONPlaceholder **simulates** writes: `POST`/`PUT`/`PATCH`/`DELETE` return realistic
success responses (e.g. `POST /posts` returns `201` with a new id like `101`), but
nothing is actually persisted server-side. Tests only assert against the response of
the call that produced it — no test chains a write to a follow-up read expecting the
change to be visible.

## Running the tests

Locally, with Maven:

```bash
mvn clean test
```

Generate and view the Allure report:

```bash
mvn allure:report
mvn allure:serve
```

With Docker:

```bash
docker build -t api-testing-lab .
docker run --rm api-testing-lab
```

CI runs the full suite and publishes the Allure report as a build artifact on every
push and pull request — see `.github/workflows/api-tests.yml`.
