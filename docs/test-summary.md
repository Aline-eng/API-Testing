# Test Summary

Automated REST Assured test suite against the [JSONPlaceholder](https://jsonplaceholder.typicode.com)
API, covering full CRUD (`GET`/`POST`/`PUT`/`PATCH`/`DELETE`) on all six
JSONPlaceholder resources: `/posts`, `/comments`, `/albums`, `/photos`, `/todos`, and
`/users`. Tests are organized one package per resource
(`org.automation.tests.posts`, `.comments`, `.albums`, `.photos`, `.todos`, `.users`).

## Results

51 / 51 tests passing (verified locally with `mvn clean test`, inside the Docker
container, and in GitHub Actions CI).

| Test class | Tests | What it covers |
|---|---|---|
| `posts.PostsGetTest` | 4 | List all (200, 100 items, schema-valid, < 2s), single by id, 404, filter by `userId` |
| `posts.PostsPostTest` | 1 | `POST /posts` (201, echoes submitted fields, generated id present) |
| `posts.PostsPutPatchTest` | 2 | `PUT`/`PATCH /posts/{id}` (full replace / partial update) |
| `posts.PostsDeleteTest` | 1 | `DELETE /posts/{id}` (200, empty body) |
| `posts.PostsEdgeCasesTest` | 3 | Malformed/edge-case POST bodies (see findings below) |
| `comments.CommentsGetTest` | 4 | Nested list (`/posts/{id}/comments`), filter by `postId` (query param), single by id, 404 |
| `comments.CommentsPostTest` | 1 | `POST /comments` (201, echoes submitted fields, generated id present) |
| `comments.CommentsPutPatchTest` | 2 | `PUT`/`PATCH /comments/{id}` (full replace / partial update) |
| `comments.CommentsDeleteTest` | 1 | `DELETE /comments/{id}` (200, empty body) |
| `albums.AlbumsGetTest` | 4 | List all (200, 100 items, schema-valid), single by id, 404, filter by `userId` |
| `albums.AlbumsPostTest` | 1 | `POST /albums` (201, echoes submitted fields, generated id present) |
| `albums.AlbumsPutPatchTest` | 2 | `PUT`/`PATCH /albums/{id}` (full replace / partial update) |
| `albums.AlbumsDeleteTest` | 1 | `DELETE /albums/{id}` (200, empty body) |
| `photos.PhotosGetTest` | 4 | List all (200, 5000 items, 25-item schema sample), single by id, 404, filter by `albumId` |
| `photos.PhotosPostTest` | 1 | `POST /photos` (201, echoes submitted fields, generated id present) |
| `photos.PhotosPutPatchTest` | 2 | `PUT`/`PATCH /photos/{id}` (full replace / partial update) |
| `photos.PhotosDeleteTest` | 1 | `DELETE /photos/{id}` (200, empty body) |
| `todos.TodosGetTest` | 5 | List all (200, 200 items, schema-valid), single by id, 404, filter by `userId`, filter by `completed` |
| `todos.TodosPostTest` | 1 | `POST /todos` (201, echoes submitted fields, generated id present) |
| `todos.TodosPutPatchTest` | 2 | `PUT`/`PATCH /todos/{id}` (full replace / partial update) |
| `todos.TodosDeleteTest` | 1 | `DELETE /todos/{id}` (200, empty body) |
| `users.UsersGetTest` | 3 | Single by id (nested `address`/`company`), 404, filter by `username` |
| `users.UsersPostTest` | 1 | `POST /users` (201, echoes submitted fields, generated id present) |
| `users.UsersPutPatchTest` | 2 | `PUT`/`PATCH /users/{id}` (full replace / partial update, nested objects unaffected by PATCH) |
| `users.UsersDeleteTest` | 1 | `DELETE /users/{id}` (200, empty body) |

Every test asserts a status code, at least one header, and relevant body fields; GET
tests additionally validate the response against a JSON schema
(`src/test/resources/schemas/`), and every resource's GET coverage includes a
list-all, a single-by-id, a 404-for-invalid-id, and at least one query-parameter
filter test.

**Notes on scope decisions:**
- The original lab spec labeled Comments and Users as "read-only" (`GET` only) and
  didn't mention Albums/Photos/Todos at all; CRUD was expanded to all six resources
  per later scope decisions, since JSONPlaceholder simulates writes identically
  across every resource.
- Security/penetration testing was explicitly considered and left out of scope —
  JSONPlaceholder has no real backend, so classic exploits (injection, auth bypass)
  don't apply. Two real, non-exploit findings surfaced anyway and are documented
  below: the malformed-JSON 500 response leaks an internal stack trace, and no
  endpoint enforces authentication (expected for a public demo API, but worth
  stating explicitly rather than leaving implicit).

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

- `mvn clean test` — run all 51 tests
- `mvn allure:report && mvn allure:serve` — generate and view the Allure report
  (request/response attached per test via the `allure-rest-assured` filter)
- `docker build -t api-testing-lab . && docker run --rm api-testing-lab` — run the
  suite in a container
- CI runs the same suite plus report generation on every push/PR; the Allure report
  is published as a downloadable build artifact
