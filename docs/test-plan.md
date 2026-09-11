# API Test Plan — JSONPlaceholder REST API

| | |
|---|---|
| **Project** | API Testing Lab — REST Assured |
| **Author** | Aline Nzikwinkunda |
| **Status** | Final |
| **Related documents** | [README.md](../README.md), [test-summary.md](test-summary.md) |

## 1. Introduction

This document defines the test strategy for the automated API test suite covering
the [JSONPlaceholder](https://jsonplaceholder.typicode.com) REST API, developed as
part of the QA Specialization training module *API Testing with REST Assured*. It
describes the scope, approach, environment, and test cases used to validate CRUD
operations, response correctness, and reporting.

## 2. Objective

Validate the functional correctness of all six JSONPlaceholder resources
(`/posts`, `/comments`, `/albums`, `/photos`, `/todos`, `/users`) through automated
test cases covering Create, Read, Update, and Delete operations, verifying status
codes, response bodies, headers, and JSON schema compliance.

## 3. Scope

### In Scope

- Full CRUD (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) on all six JSONPlaceholder
  resources: `/posts`, `/comments`, `/albums`, `/photos`, `/todos`, `/users`
- Status code, header, and response body validation on every test
- JSON schema validation for `GET` responses
- `GET` coverage beyond a single by-id lookup: list-all, single-by-id,
  non-existent-id (`404`), and query-parameter filtering (e.g. `?userId=`,
  `?postId=`, `?albumId=`, `?completed=`, `?username=`) for every resource that
  supports it
- Negative and edge-case handling: non-existent resources, malformed payloads, and
  semantically invalid request bodies
- Allure reporting, Docker containerization, GitHub Actions CI/CD, and GitHub Pages
  report publishing

### Out of Scope

- Load and performance testing, beyond a single response-time assertion on
  `GET /posts`
- Authentication and authorization testing — the API requires no authentication
- UI testing — this is an API-only test suite
- Security and penetration testing (e.g. injection, authentication bypass).
  JSONPlaceholder is a public mock API with no persistent backend, so exploit-style
  security testing does not meaningfully apply. See `docs/test-summary.md` for the
  two API-behavior observations that were noted as part of this assessment.

## 4. Test Approach

- **Framework:** REST Assured 5.x with JUnit 5 (Jupiter) and Hamcrest matchers.
- **Structure:** one test package per resource (`org.automation.tests.posts`,
  `.comments`, `.albums`, `.photos`, `.todos`, `.users`), each containing that
  resource's full CRUD test classes.
- **Positive cases:** one test class per HTTP method group per resource, each
  asserting status code, at least one response header, and relevant body fields.
- **GET coverage:** each resource's `GET` tests cover the full collection (with a
  count assertion), a single-resource lookup, a `404` for a non-existent id, and at
  least one query-parameter filter, confirmed against live API responses.
- **Schema validation:** every `GET` response is validated against a JSON Schema
  defined in `src/test/resources/schemas/`, authored from observed API responses.
  For `/photos` (5,000 records), a 25-item sample is schema-validated per run to
  keep execution time reasonable, while the full record count is still asserted.
- **Negative and edge cases:** invalid resource ids (`404`), malformed JSON syntax,
  and semantically invalid payloads (empty body, incorrect field types) are
  asserted against the status code actually returned by the API.
- **Write-safety constraint:** JSONPlaceholder simulates write operations without
  persisting them. No test chains a write to a subsequent read expecting the change
  to be reflected; each write test asserts only against the response it produced.
- **Test data management:** request-body fixtures are defined in
  `src/test/resources/testdata/` (one JSON file per resource) and loaded via a
  shared `TestData` utility, serving as the single source of truth for both the
  request sent and the response fields asserted.

## 5. Test Environment

| Component | Version / Detail |
|---|---|
| Language / Build | Java 21, Maven 3.9 |
| Test framework | JUnit 5.10.2 (Jupiter) |
| API client | REST Assured 5.4.0, json-schema-validator |
| Assertions | Hamcrest |
| Serialization | Jackson Databind 2.17.0 |
| Reporting | Allure 2.27.0 (allure-junit5, allure-rest-assured) |
| Containerization | Docker, `maven:3.9-eclipse-temurin-21` |
| CI/CD | GitHub Actions (`ubuntu-latest`) |
| Target API | `https://jsonplaceholder.typicode.com` (public, no authentication) |

## 6. Entry Criteria

- `pom.xml` configured with all required dependencies and plugins.
- Shared base test configuration (`BaseTest`) providing the base URI and
  request/response specifications.
- Target API reachable, with response shapes captured for schema authoring.

## 7. Exit Criteria

- All planned test cases (Section 9) implemented and passing.
- Allure report generated with request/response detail for every test.
- Docker image builds and executes the full suite successfully.
- CI/CD pipeline runs the suite inside Docker and publishes the Allure report on
  every push and pull request.

## 8. Test Deliverables

- This test plan (`docs/test-plan.md`)
- Automated test suite (`src/test/java/org/automation/tests/`)
- JSON schemas (`src/test/resources/schemas/`)
- `Dockerfile`
- CI/CD workflow (`.github/workflows/api-tests.yml`)
- Allure report (published to GitHub Pages and available as a CI build artifact)
- Test summary and findings report (`docs/test-summary.md`)

## 9. Test Case Summary

51 test cases across six resources.

**Posts (11)**

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-01 | GET | `/posts` | Retrieve all posts | 200, 100 items, each schema-valid, response time < 2s | Positive |
| TC-02 | GET | `/posts/{id}` | Retrieve a single post | 200, `id`/`userId` match, schema-valid | Positive |
| TC-03 | GET | `/posts/99999` | Retrieve a non-existent post | 404 | Negative |
| TC-04 | GET | `/posts?userId={id}` | Filter posts by userId | 200, only that user's posts (10 items) | Positive |
| TC-05 | POST | `/posts` | Create a post | 201, echoes submitted fields, generated `id` present | Positive |
| TC-06 | PUT | `/posts/{id}` | Full update of a post | 200, full resource replaced | Positive |
| TC-07 | PATCH | `/posts/{id}` | Partial update of a post | 200, only the targeted field changes | Positive |
| TC-08 | DELETE | `/posts/{id}` | Delete a post | 200, empty response body | Positive |
| TC-09 | POST | `/posts` | Malformed JSON syntax | 500 (see Section 11, R-2) | Negative |
| TC-10 | POST | `/posts` | Empty JSON object body | 201 (no server-side validation) | Negative/Edge |
| TC-11 | POST | `/posts` | Wrong field types (`userId` as string) | 201 (no server-side validation) | Negative/Edge |

**Comments (8)**

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-12 | GET | `/posts/{id}/comments` | Retrieve comments for a post (nested route) | 200, non-empty, every `postId` matches, schema-valid | Positive |
| TC-13 | GET | `/comments?postId={id}` | Filter comments by postId (query parameter) | 200, same 5 comments as the nested route | Positive |
| TC-14 | GET | `/comments/{id}` | Retrieve a single comment | 200, `id` matches, schema-valid | Positive |
| TC-15 | GET | `/comments/99999` | Retrieve a non-existent comment | 404 | Negative |
| TC-16 | POST | `/comments` | Create a comment | 201, echoes submitted fields, generated `id` present | Positive |
| TC-17 | PUT | `/comments/{id}` | Full update of a comment | 200, full resource replaced | Positive |
| TC-18 | PATCH | `/comments/{id}` | Partial update of a comment | 200, only the targeted field changes | Positive |
| TC-19 | DELETE | `/comments/{id}` | Delete a comment | 200, empty response body | Positive |

**Albums (8)**

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-20 | GET | `/albums` | Retrieve all albums | 200, 100 items, each schema-valid | Positive |
| TC-21 | GET | `/albums/{id}` | Retrieve a single album | 200, `id`/`userId` match, schema-valid | Positive |
| TC-22 | GET | `/albums/99999` | Retrieve a non-existent album | 404 | Negative |
| TC-23 | GET | `/albums?userId={id}` | Filter albums by userId | 200, only that user's albums (10 items) | Positive |
| TC-24 | POST | `/albums` | Create an album | 201, echoes submitted fields, generated `id` present | Positive |
| TC-25 | PUT | `/albums/{id}` | Full update of an album | 200, full resource replaced | Positive |
| TC-26 | PATCH | `/albums/{id}` | Partial update of an album | 200, only the targeted field changes | Positive |
| TC-27 | DELETE | `/albums/{id}` | Delete an album | 200, empty response body | Positive |

**Photos (8)**

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-28 | GET | `/photos` | Retrieve all photos | 200, 5000 items, 25-item sample schema-valid | Positive |
| TC-29 | GET | `/photos/{id}` | Retrieve a single photo | 200, `id`/`albumId` match, schema-valid | Positive |
| TC-30 | GET | `/photos/99999999` | Retrieve a non-existent photo | 404 | Negative |
| TC-31 | GET | `/photos?albumId={id}` | Filter photos by albumId | 200, only that album's photos (50 items) | Positive |
| TC-32 | POST | `/photos` | Create a photo | 201, echoes submitted fields, generated `id` present | Positive |
| TC-33 | PUT | `/photos/{id}` | Full update of a photo | 200, full resource replaced | Positive |
| TC-34 | PATCH | `/photos/{id}` | Partial update of a photo | 200, only the targeted field changes | Positive |
| TC-35 | DELETE | `/photos/{id}` | Delete a photo | 200, empty response body | Positive |

**Todos (9)**

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-36 | GET | `/todos` | Retrieve all todos | 200, 200 items, each schema-valid | Positive |
| TC-37 | GET | `/todos/{id}` | Retrieve a single todo | 200, `id`/`userId`/`completed` match, schema-valid | Positive |
| TC-38 | GET | `/todos/99999` | Retrieve a non-existent todo | 404 | Negative |
| TC-39 | GET | `/todos?userId={id}` | Filter todos by userId | 200, only that user's todos (20 items) | Positive |
| TC-40 | GET | `/todos?completed=true` | Filter todos by completed status | 200, only completed todos | Positive |
| TC-41 | POST | `/todos` | Create a todo | 201, echoes submitted fields, generated `id` present | Positive |
| TC-42 | PUT | `/todos/{id}` | Full update of a todo | 200, full resource replaced | Positive |
| TC-43 | PATCH | `/todos/{id}` | Partial update of a todo | 200, only the targeted field changes | Positive |
| TC-44 | DELETE | `/todos/{id}` | Delete a todo | 200, empty response body | Positive |

**Users (7)**

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-45 | GET | `/users/{id}` | Retrieve a single user | 200, nested `address`/`company` present and correctly typed, schema-valid | Positive |
| TC-46 | GET | `/users/99999` | Retrieve a non-existent user | 404 | Negative |
| TC-47 | GET | `/users?username={name}` | Filter users by username | 200, only the matching user | Positive |
| TC-48 | POST | `/users` | Create a user | 201, echoes submitted fields, generated `id` present | Positive |
| TC-49 | PUT | `/users/{id}` | Full update of a user | 200, full resource replaced | Positive |
| TC-50 | PATCH | `/users/{id}` | Partial update of a user | 200, only the targeted field changes, nested `address`/`company` unaffected | Positive |
| TC-51 | DELETE | `/users/{id}` | Delete a user | 200, empty response body | Positive |

## 10. Roles and Responsibilities

| Role | Responsibility | Assigned to |
|---|---|---|
| QA Engineer | Test planning, automation development, execution, and reporting | Aline Nzikwinkunda |

## 11. Risks and Assumptions

| ID | Risk / Assumption | Impact | Mitigation |
|---|---|---|---|
| R-1 | JSONPlaceholder simulates writes; no data persists server-side | Tests that assume persistence would fail or produce false results | No test chains a write to a subsequent read; each write test asserts only against its own response |
| R-2 | The API returns `500` for malformed JSON syntax and `201` for semantically invalid payloads, rather than `400`, since it performs no server-side payload validation | Tests written against an assumed `4xx` response would fail | Tests assert the exact status code observed from the live API; behavior documented in `docs/test-summary.md` |
| R-3 | The API is public and unauthenticated, and may be subject to network flakiness or rate limiting outside this project's control | Intermittent test failures unrelated to code defects | No rate limiting was observed during test development or execution |
| R-4 | JDK version mismatch between the initially provided Docker/CI templates (JDK 17) and the project's Java 21 target | Docker build and CI pipeline would fail, since JDK 17 cannot compile Java 21 bytecode | Standardized on JDK 21 across the Dockerfile and CI workflow |

## 12. Test Execution

See the [README](../README.md) for commands to run the suite locally, in Docker,
and via CI. Execution results and findings from the most recent run are recorded in
[`docs/test-summary.md`](test-summary.md).
