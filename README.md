# API Testing Lab — REST Assured

An automated test suite for the [JSONPlaceholder](https://jsonplaceholder.typicode.com)
REST API, developed for the QA Specialization training module *API Testing with
REST Assured*. The suite validates full CRUD behavior (`GET`, `POST`, `PUT`, `PATCH`,
`DELETE`) across all six JSONPlaceholder resources — `/posts`, `/comments`,
`/albums`, `/photos`, `/todos`, `/users` — with query-parameter filtering, JSON
schema validation, Allure reporting, Docker containerization, and a GitHub Actions
CI/CD pipeline.

**Live Allure report:** https://aline-eng.github.io/API-Testing/
Republished automatically by CI on every push to `main`.

## Tech Stack

| Category | Technology |
|---|---|
| Language / Build | Java 21, Maven |
| API testing | REST Assured 5.x, JSON Schema Validator |
| Test framework | JUnit 5 (Jupiter), Hamcrest |
| Data handling | Jackson Databind (JSON test-data fixtures, schema-validation serialization) |
| Reporting | Allure (JUnit 5 + REST Assured integration) |
| Containerization | Docker |
| CI/CD | GitHub Actions |

## Project Structure

```
src/test/java/org/automation/
  base/
    BaseTest.java      shared base URI and request/response specification
    TestData.java      loads JSON fixtures from resources/testdata
  tests/
    posts/             CRUD tests for /posts
    comments/          CRUD tests for /comments
    albums/            CRUD tests for /albums
    photos/            CRUD tests for /photos
    todos/             CRUD tests for /todos
    users/             CRUD tests for /users

src/test/resources/
  config.properties    base.uri - the target environment's base URL
  schemas/             JSON Schema definitions used for response validation
  testdata/            request-body fixtures, one JSON file per resource

docs/                  test plan and test summary reports
.github/workflows/     CI/CD pipeline definition
Dockerfile
```

### Design Notes

- Assertions are made directly against the response using REST Assured's
  `jsonPath()` and Hamcrest matchers rather than through POJOs.
- Endpoint paths (e.g. `/posts/{id}`) are defined inline in each test, since they
  describe what the test verifies rather than environment configuration. Only the
  base URI (`config.properties`) and request-body fixtures (`testdata/*.json`) are
  externalized.
- Request-body fixtures are the single source of truth for both the request sent
  and the response asserted: test methods read expected field values back from the
  same fixture object used to build the request body, avoiding duplicated literals.
- One fixture cannot be externalized: `PostsEdgeCasesTest` includes a deliberately
  malformed JSON string used to test the API's handling of invalid syntax. Since it
  is not valid JSON, it cannot be stored in a `.json` fixture file and remains a
  string literal in the test class.

## API Behavior Note

JSONPlaceholder simulates write operations rather than persisting them. `POST`,
`PUT`, `PATCH`, and `DELETE` requests return realistic success responses (for
example, `POST /posts` returns `201` with a newly generated id), but no data is
actually stored server-side. Accordingly, no test chains a write operation to a
subsequent read expecting the change to be reflected — each write test asserts only
against the response it produced.

## Running the Tests

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

### CI/CD

On every push and pull request, the pipeline builds the Docker image, runs the full
suite inside the container, and publishes the Allure report as a downloadable build
artifact. On a successful push to `main`, the report is also deployed to GitHub
Pages at https://aline-eng.github.io/API-Testing/. See
`.github/workflows/api-tests.yml` for the full pipeline definition.

The Dockerfile and CI workflow both target JDK 21 (`maven:3.9-eclipse-temurin-21`,
`java-version: '21'`), matching the Java 21 target defined in `pom.xml`.

## Documentation

| Document | Contents |
|---|---|
| [docs/test-plan.md](docs/test-plan.md) | Scope, approach, environment, and the full test case matrix |
| [docs/test-summary.md](docs/test-summary.md) | Execution results and documented API behavior findings |
