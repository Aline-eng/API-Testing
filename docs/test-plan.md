# API Test Plan — JSONPlaceholder REST API

## 1. Introduction

This document defines the test strategy for automating API testing of the
[JSONPlaceholder](https://jsonplaceholder.typicode.com) REST API using REST Assured,
as part of a QA Specialization training lab. It covers CRUD operation testing,
response validation, reporting, containerization, and CI/CD integration.

## 2. Objective

Validate the functional correctness of all six JSONPlaceholder resources
(`/posts`, `/comments`, `/albums`, `/photos`, `/todos`, `/users`) by automating test
cases for Create, Read, Update, and Delete operations, verifying status codes,
response bodies, headers, and JSON schema compliance.

## 3. Scope

**In scope:**
- Full CRUD (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) on all 6 JSONPlaceholder
  resources: `/posts`, `/comments`, `/albums`, `/photos`, `/todos`, `/users`
- Status code, header, and response body validation for every test
- JSON schema validation for `GET` responses
- `GET` coverage beyond simple by-id lookups: list-all, single-by-id, non-existent-id
  (404), and query-parameter filtering (e.g. `?userId=`, `?postId=`, `?albumId=`,
  `?completed=`, `?username=`) on every resource that supports it
- Negative/edge cases: non-existent resource (404), malformed and semantically
  invalid POST payloads
- Allure reporting, Docker containerization, GitHub Actions CI/CD, GitHub Pages
  report publishing

**Out of scope:**
- Load/performance testing beyond a single response-time assertion on `GET /posts`
- Authentication/authorization testing — the API requires none
- UI testing — this is an API-only test suite
- Security/penetration testing (injection, auth bypass, etc.) — JSONPlaceholder has
  no real backend to meaningfully attack; see `docs/test-summary.md` for the
  reasoning behind this exclusion

## 4. Test Approach

- **Framework:** REST Assured 5.x + JUnit 5 (Jupiter), Hamcrest matchers
- **Structure:** one test package per resource (`org.automation.tests.posts`,
  `.comments`, `.albums`, `.photos`, `.todos`, `.users`), grouping each resource's
  full CRUD test classes together
- **Positive cases:** one test class per HTTP verb group per resource, asserting
  status code, at least one header, and relevant body fields
- **GET depth:** beyond a single "get by id" test, GET coverage includes listing
  the full collection (with a count assertion), a 404 for a non-existent id, and at
  least one query-parameter filter test per resource (e.g. `GET /posts?userId=1`,
  `GET /todos?completed=true`) — filtering was verified against the live API before
  writing each test
- **Schema validation:** every `GET` response validated against a JSON Schema
  (`src/test/resources/schemas/`), authored from real API responses. For `/photos`
  (5,000 items), only a 25-item sample is schema-validated per run to keep the test
  fast, while the full count is still asserted
- **Negative/edge cases:** invalid resource id (404), malformed JSON syntax, and
  semantically invalid payloads (empty body, wrong field types) — asserting the
  exact status code actually observed from the live API, not an assumed one
- **Write-safety constraint:** JSONPlaceholder simulates writes without persisting
  them, so no test chains a write to a follow-up read expecting the change to be
  visible — each write test only asserts against the response it produced

## 5. Test Environment

| Component | Version / Detail |
|---|---|
| Language / Build | Java 21, Maven 3.9 |
| Test framework | JUnit 5.10.2 (Jupiter) |
| API client | REST Assured 5.4.0 + json-schema-validator |
| Assertions | Hamcrest |
| Serialization | Jackson Databind 2.17.0 |
| Reporting | Allure 2.27.0 (allure-junit5, allure-rest-assured) |
| Containerization | Docker, `maven:3.9-eclipse-temurin-21` |
| CI/CD | GitHub Actions (`ubuntu-latest`) |
| Target API | `https://jsonplaceholder.typicode.com` (public, no authentication) |

## 6. Entry Criteria

- `pom.xml` configured with all required dependencies and plugins
- Shared base test configuration (`BaseTest`) providing base URI and request/response
  specs
- Target API reachable and its response shapes captured for schema authoring

## 7. Exit Criteria

- All planned test cases (see §9) implemented and passing
- Allure report generated with request/response detail for every test
- Docker image builds and runs the full suite successfully
- CI/CD pipeline runs the suite inside Docker and publishes the Allure report on
  every push/PR

## 8. Test Deliverables

- This test plan (`docs/test-plan.md`)
- Automated test suite (`src/test/java/org/automation/tests/`)
- JSON schemas (`src/test/resources/schemas/`)
- `Dockerfile`
- CI/CD workflow (`.github/workflows/api-tests.yml`)
- Allure report (generated locally or downloaded as a CI build artifact)
- Test summary / results and findings report (`docs/test-summary.md`)

## 9. Test Case Summary

51 test cases across all 6 resources.

**Posts** (11)

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
| TC-09 | POST | `/posts` | Malformed JSON syntax | 500 — documented API deviation from REST convention | Negative |
| TC-10 | POST | `/posts` | Empty JSON object body | 201 — API performs no server-side validation | Negative/Edge |
| TC-11 | POST | `/posts` | Wrong field types (`userId` as string) | 201 — API performs no server-side validation | Negative/Edge |

**Comments** (8)

| TC ID | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|
| TC-12 | GET | `/posts/{id}/comments` | Retrieve comments for a post (nested route) | 200, non-empty, every `postId` matches, schema-valid | Positive |
| TC-13 | GET | `/comments?postId={id}` | Filter comments by postId (query param) | 200, same 5 comments as the nested route | Positive |
| TC-14 | GET | `/comments/{id}` | Retrieve a single comment | 200, `id` matches, schema-valid | Positive |
| TC-15 | GET | `/comments/99999` | Retrieve a non-existent comment | 404 | Negative |
| TC-16 | POST | `/comments` | Create a comment | 201, echoes submitted fields, generated `id` present | Positive |
| TC-17 | PUT | `/comments/{id}` | Full update of a comment | 200, full resource replaced | Positive |
| TC-18 | PATCH | `/comments/{id}` | Partial update of a comment | 200, only the targeted field changes | Positive |
| TC-19 | DELETE | `/comments/{id}` | Delete a comment | 200, empty response body | Positive |

**Albums** (8)

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

**Photos** (8)

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

**Todos** (9)

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

**Users** (7)

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

Single QA engineer role (Aline Nzikwinkunda): test planning, automation
implementation, execution, and reporting.

## 11. Risks and Assumptions

- **JSONPlaceholder simulates writes** — `POST`/`PUT`/`PATCH`/`DELETE` return
  realistic success responses but nothing persists server-side. Mitigated by never
  chaining a write to a follow-up read.
- **No server-side payload validation** — malformed or semantically invalid POST
  bodies do not reliably produce `4xx` responses (observed: `500` for syntax errors,
  `201` for structurally valid-but-empty/wrong-typed bodies). Documented as a known
  API deviation rather than worked around.
- **Public, unauthenticated API** — subject to network flakiness and rate limiting
  outside this project's control; no rate limiting was encountered during test runs.
- **Java version mismatch in originally-circulated Docker/CI templates** (JDK 17)
  versus the project's Java 21 target — resolved by standardizing on JDK 21 across
  the Dockerfile and CI workflow.

## 12. Test Execution

See the [README](../README.md) for exact commands to run the suite locally, in
Docker, and via CI. Results and findings from the most recent execution are recorded
in [`docs/test-summary.md`](test-summary.md).
