# Test Summary

Execution results for the automated test suite covering all six JSONPlaceholder
resources: `/posts`, `/comments`, `/albums`, `/photos`, `/todos`, and `/users`. See
[test-plan.md](test-plan.md) for scope, approach, and the full test case matrix.

## Results

**51 / 51 tests passing**, verified locally (`mvn clean test`), inside the Docker
container, and in GitHub Actions CI.

| Test class | Tests | Coverage |
|---|---|---|
| `posts.PostsGetTest` | 4 | List all (200, 100 items, schema-valid, < 2s), single by id, 404, filter by `userId` |
| `posts.PostsPostTest` | 1 | Create (201, echoes submitted fields, generated id present) |
| `posts.PostsPutPatchTest` | 2 | Full update / partial update |
| `posts.PostsDeleteTest` | 1 | Delete (200, empty body) |
| `posts.PostsEdgeCasesTest` | 3 | Malformed and semantically invalid payloads (see Findings) |
| `comments.CommentsGetTest` | 4 | Nested list (`/posts/{id}/comments`), filter by `postId`, single by id, 404 |
| `comments.CommentsPostTest` | 1 | Create (201, echoes submitted fields, generated id present) |
| `comments.CommentsPutPatchTest` | 2 | Full update / partial update |
| `comments.CommentsDeleteTest` | 1 | Delete (200, empty body) |
| `albums.AlbumsGetTest` | 4 | List all (200, 100 items, schema-valid), single by id, 404, filter by `userId` |
| `albums.AlbumsPostTest` | 1 | Create (201, echoes submitted fields, generated id present) |
| `albums.AlbumsPutPatchTest` | 2 | Full update / partial update |
| `albums.AlbumsDeleteTest` | 1 | Delete (200, empty body) |
| `photos.PhotosGetTest` | 4 | List all (200, 5000 items, 25-item schema sample), single by id, 404, filter by `albumId` |
| `photos.PhotosPostTest` | 1 | Create (201, echoes submitted fields, generated id present) |
| `photos.PhotosPutPatchTest` | 2 | Full update / partial update |
| `photos.PhotosDeleteTest` | 1 | Delete (200, empty body) |
| `todos.TodosGetTest` | 5 | List all (200, 200 items, schema-valid), single by id, 404, filter by `userId`, filter by `completed` |
| `todos.TodosPostTest` | 1 | Create (201, echoes submitted fields, generated id present) |
| `todos.TodosPutPatchTest` | 2 | Full update / partial update |
| `todos.TodosDeleteTest` | 1 | Delete (200, empty body) |
| `users.UsersGetTest` | 3 | Single by id (nested `address`/`company`), 404, filter by `username` |
| `users.UsersPostTest` | 1 | Create (201, echoes submitted fields, generated id present) |
| `users.UsersPutPatchTest` | 2 | Full update / partial update (nested objects unaffected by PATCH) |
| `users.UsersDeleteTest` | 1 | Delete (200, empty body) |

Every test asserts a status code, at least one header, and relevant body fields.
`GET` tests additionally validate the response against a JSON schema
(`src/test/resources/schemas/`); every resource's `GET` coverage includes a
list-all, a single-by-id, a 404-for-invalid-id, and at least one query-parameter
filter test.

## Scope Notes

- CRUD coverage spans all six JSONPlaceholder resources. JSONPlaceholder simulates
  write operations identically across every resource, so the same test pattern
  (list, single, create, update, delete) applies uniformly.
- Security and penetration testing is out of scope (see `test-plan.md`, Section 3),
  since JSONPlaceholder has no persistent backend to meaningfully target. Two
  API-behavior observations were noted during test development and are recorded
  under Findings below: an internal stack trace is exposed in one error response,
  and no endpoint requires authentication.

## Findings

**F-1 — Writes are simulated, not persisted.** `POST`, `PUT`, `PATCH`, and `DELETE`
return realistic success responses (for example, a newly created post is assigned
id `101`), but no data is stored server-side. No test chains a write to a
subsequent read expecting the change to be reflected; each write test asserts only
against the response it produced.

**F-2 — Malformed payloads do not return 4xx.** The API performs no server-side
request validation:
- Syntactically invalid JSON (e.g. an unterminated request body) causes an
  unhandled exception in the API's body-parser, returning **500** rather than 400.
- Structurally valid but semantically invalid payloads (an empty object, or a field
  sent with the wrong type) are accepted and return **201**.

`PostsEdgeCasesTest` asserts the exact status codes observed from the live API
(logging the raw response for each case) rather than assuming a `4xx` response.

**F-3 — Information disclosure in error responses.** The `500` response in F-2
includes a full stack trace, exposing internal file paths and framework/version
details (Express, body-parser). This is a genuine information-disclosure pattern,
noted for completeness; it is not exploitable in this context since the API is a
public mock service with no sensitive backend.

**F-4 — No authentication is enforced on any endpoint.** All CRUD operations,
including writes, succeed without credentials. Expected behavior for a public demo
API, and stated explicitly since a real-world equivalent would require
authentication and authorization on write operations.

**F-5 — Dependency version conflict.** `allure-junit5` transitively pulls
`junit-jupiter-api`/`junit-platform` `5.9.2`, conflicting with the directly
declared `junit-jupiter 5.10.2` and causing test discovery to fail
(`NoClassDefFoundError: TempDirFactory`). Resolved by pinning the JUnit 5 BOM in
`pom.xml`'s `dependencyManagement`.

**F-6 — JDK version mismatch in initial Docker/CI templates.** The Docker and CI
templates initially provided for this project targeted JDK 17, while `pom.xml`
targets Java 21; a JDK 17 toolchain cannot compile Java 21 bytecode. Resolved by
standardizing on JDK 21 across the Dockerfile and CI workflow.

## Running the Suite

See the [README](../README.md) for local, Docker, and CI instructions.

- `mvn clean test` — run all 51 tests
- `mvn allure:report && mvn allure:serve` — generate and view the Allure report
  (request/response detail attached per test via the `allure-rest-assured` filter)
- `docker build -t api-testing-lab . && docker run --rm api-testing-lab` — run the
  suite in a container
- CI runs the same suite inside Docker on every push and pull request, publishes
  the Allure report as a build artifact, and deploys it to GitHub Pages on a
  successful push to `main`
