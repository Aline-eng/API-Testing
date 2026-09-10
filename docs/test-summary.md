# Test Summary

Automated REST Assured test suite against the [JSONPlaceholder](https://jsonplaceholder.typicode.com)
API, covering full CRUD on `/posts`, relational reads on `/comments`, and nested-object
reads on `/users`.

## Results

12 / 12 tests passing (verified locally with `mvn clean test`, inside the Docker
container, and in GitHub Actions CI).

| Test class | Tests | What it covers |
|---|---|---|
| `PostsGetTest` | 3 | `GET /posts` (200, 100 items, schema-valid, < 2s), `GET /posts/{id}` (200, id/userId match, schema valid), `GET /posts/99999` (404) |
| `CommentsGetTest` | 1 | `GET /posts/{id}/comments` (200, non-empty, every `postId` matches, schema valid) |
| `UsersGetTest` | 1 | `GET /users/{id}` (200, nested `address`/`company` present and correctly typed, schema valid) |
| `PostsPostTest` | 1 | `POST /posts` (201, echoes submitted fields, generated id present) |
| `PostsPutPatchTest` | 2 | `PUT /posts/{id}` (200, full replace), `PATCH /posts/{id}` (200, only targeted field changes) |
| `PostsDeleteTest` | 1 | `DELETE /posts/{id}` (200, empty body) |
| `PostsEdgeCasesTest` | 3 | Malformed/edge-case POST bodies (see findings below) |

Every test asserts a status code, at least one header, and relevant body fields; GET
tests additionally validate the response against a JSON schema
(`src/test/resources/schemas/`).

## Known constraints and findings

**JSONPlaceholder simulates writes.** `POST`/`PUT`/`PATCH`/`DELETE` return realistic
success responses (e.g. a new post gets id `101`), but nothing is persisted
server-side. No test chains a write to a follow-up read expecting the change to be
visible — each write test only asserts against the response it produced.

**Malformed POST bodies do not produce 4xx.** The original test-coverage matrix
called for "malformed POST body → appropriate 4xx handling," but the live API
doesn't behave that way:
- Syntactically invalid JSON (e.g. an unclosed body) crashes JSONPlaceholder's
  `body-parser` with an unhandled `JSON.parse` exception, returning **500**, not 400.
- Structurally valid but semantically empty/wrong-typed bodies (`{}`, `userId` sent
  as a string) are accepted with **201** — the API performs no server-side payload
  validation at all.

`PostsEdgeCasesTest` documents and asserts these exact, observed status codes
(logging the raw response first) rather than relaxing the assertions to a status
range or silently working around the mismatch.

**JUnit version conflict in the dependency tree.** `allure-junit5` transitively
pulls `junit-jupiter-api`/`junit-platform` `5.9.2`, which conflicts with the
directly-declared `junit-jupiter 5.10.2` and broke test discovery
(`NoClassDefFoundError: TempDirFactory`) until the whole JUnit 5 BOM was pinned in
`pom.xml`'s `dependencyManagement`.

**Java version mismatch in the original Docker/CI spec.** The Dockerfile and
GitHub Actions workflow templates given for this lab pinned JDK 17, while the
pom.xml (Java 21) requires it — a JDK 17 toolchain cannot compile Java 21
bytecode. Both were changed to JDK 21 to match the pom.

## Running the suite

See the [README](../README.md) for local, Docker, and CI instructions.

- `mvn clean test` — run all 12 tests
- `mvn allure:report && mvn allure:serve` — generate and view the Allure report
  (request/response attached per test via the `allure-rest-assured` filter)
- `docker build -t api-testing-lab . && docker run --rm api-testing-lab` — run the
  suite in a container
- CI runs the same suite plus report generation on every push/PR; the Allure report
  is published as a downloadable build artifact
