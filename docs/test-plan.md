# API Test Plan — JSONPlaceholder REST API

## 1. Introduction

This document defines the test strategy for automating API testing of the
[JSONPlaceholder](https://jsonplaceholder.typicode.com) REST API using REST Assured,
as part of a QA Specialization training lab. It covers CRUD operation testing,
response validation, reporting, containerization, and CI/CD integration.

## 2. Objective

Validate the functional correctness of JSONPlaceholder's `/posts`, `/comments`, and
`/users` endpoints by automating test cases for Create, Read, Update, and Delete
operations, verifying status codes, response bodies, headers, and JSON schema
compliance.

## 3. Scope

**In scope:**
- Full CRUD (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) on `/posts`, `/comments`, and
  `/users`
- Status code, header, and response body validation for every test
- JSON schema validation for `GET` responses
- Negative/edge cases: non-existent resource (404), malformed and semantically
  invalid POST payloads
- Allure reporting, Docker containerization, GitHub Actions CI/CD

**Out of scope:**
- Other JSONPlaceholder resources (`/albums`, `/photos`, `/todos`) — not required
  by the lab
- Load/performance testing beyond a single response-time assertion on `GET /posts`
- Authentication/authorization testing — the API requires none
- UI testing — this is an API-only test suite

## 4. Test Approach

- **Framework:** REST Assured 5.x + JUnit 5 (Jupiter), Hamcrest matchers
- **Structure:** one test package per resource (`org.automation.tests.posts`,
  `.comments`, `.users`), grouping each resource's full CRUD test classes together
- **Positive cases:** one test class per HTTP verb group per resource, asserting
  status code, at least one header, and relevant body fields
- **Schema validation:** every `GET` response validated against a JSON Schema
  (`src/test/resources/schemas/`), authored from real API responses
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

| TC ID | Resource | Method | Endpoint | Scenario | Expected Result | Type |
|---|---|---|---|---|---|---|
| TC-01 | Posts | GET | `/posts` | Retrieve all posts | 200, 100 items, each schema-valid, response time < 2s | Positive |
| TC-02 | Posts | GET | `/posts/{id}` | Retrieve a single post | 200, `id`/`userId` match, schema-valid | Positive |
| TC-03 | Posts | GET | `/posts/99999` | Retrieve a non-existent post | 404 | Negative |
| TC-04 | Posts | POST | `/posts` | Create a post | 201, echoes submitted fields, generated `id` present | Positive |
| TC-05 | Posts | PUT | `/posts/{id}` | Full update of a post | 200, full resource replaced | Positive |
| TC-06 | Posts | PATCH | `/posts/{id}` | Partial update of a post | 200, only the targeted field changes | Positive |
| TC-07 | Posts | DELETE | `/posts/{id}` | Delete a post | 200, empty response body | Positive |
| TC-08 | Posts | POST | `/posts` | Malformed JSON syntax | 500 — documented API deviation from REST convention | Negative |
| TC-09 | Posts | POST | `/posts` | Empty JSON object body | 201 — API performs no server-side validation | Negative/Edge |
| TC-10 | Posts | POST | `/posts` | Wrong field types (`userId` as string) | 201 — API performs no server-side validation | Negative/Edge |
| TC-11 | Comments | GET | `/posts/{id}/comments` | Retrieve comments for a post | 200, non-empty, every `postId` matches, schema-valid | Positive |
| TC-12 | Comments | POST | `/comments` | Create a comment | 201, echoes submitted fields, generated `id` present | Positive |
| TC-13 | Comments | PUT | `/comments/{id}` | Full update of a comment | 200, full resource replaced | Positive |
| TC-14 | Comments | PATCH | `/comments/{id}` | Partial update of a comment | 200, only the targeted field changes | Positive |
| TC-15 | Comments | DELETE | `/comments/{id}` | Delete a comment | 200, empty response body | Positive |
| TC-16 | Users | GET | `/users/{id}` | Retrieve a single user | 200, nested `address`/`company` present and correctly typed, schema-valid | Positive |
| TC-17 | Users | POST | `/users` | Create a user | 201, echoes submitted fields, generated `id` present | Positive |
| TC-18 | Users | PUT | `/users/{id}` | Full update of a user | 200, full resource replaced | Positive |
| TC-19 | Users | PATCH | `/users/{id}` | Partial update of a user | 200, only the targeted field changes, nested `address`/`company` unaffected | Positive |
| TC-20 | Users | DELETE | `/users/{id}` | Delete a user | 200, empty response body | Positive |

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
