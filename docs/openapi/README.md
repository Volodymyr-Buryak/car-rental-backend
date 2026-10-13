# User API documentation

The [`user-api.yaml`](./user-api.yaml) file contains the OpenAPI 3.1 contract for
`UserController`, whose base path is `/api/v1/users`.

The documented operations are:

- `GET /api/v1/users/me` — return the authenticated user's profile;
- `PATCH /api/v1/users/me` — partially update editable profile fields.

Registration and email verification are intentionally absent from the HTTP API.
Keycloak publishes those events to RabbitMQ, and the user module processes them
asynchronously.

The local server URL, including the configured servlet context path, is:

```text
http://localhost:8081/carhub
```

The API expects a Keycloak JWT in the standard authorization header:

```http
Authorization: Bearer <access-token>
```
