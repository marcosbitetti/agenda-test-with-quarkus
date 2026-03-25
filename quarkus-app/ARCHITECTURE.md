Clean Architecture overview for the Agenda project

Purpose
-------
Provide a minimal, opinionated folder and package layout to keep business rules independent from frameworks and delivery mechanisms.

Layer mapping
-------------
- `org.acme.core` - Use cases and business interfaces (ports). No framework imports.
- `org.acme.domain` - Domain models and value objects. Keep free of framework annotations while possible.
- `org.acme.adapters.persistence` - Implementations of repository ports; depends on frameworks (Hibernate/Panache, JDBC).
- `org.acme.adapters.web` - REST resources and request/response DTOs; depends on core ports.
- `org.acme.config` - Framework-specific configuration and wiring.

Guidelines
----------
- Keep transactions and framework-specific annotations inside adapters.
- Use simple interfaces (ports) for repositories and services in `org.acme.core`.
- Implement integration tests in `quarkus-app/src/test` that exercise adapters against the real Postgres instance (via Docker Compose) for acceptance tests.

Code Conventions
----------------
- Avoid hardcoded literals when the platform or the project already provides a stable symbol for the same concept.
- Prefer framework constants for protocol values:
	- HTTP methods: use `jakarta.ws.rs.HttpMethod`
	- media types: use `jakarta.ws.rs.core.MediaType`
	- header names: use `jakarta.ws.rs.core.HttpHeaders` when available
	- HTTP status codes: use `jakarta.ws.rs.core.Response.Status`
- Prefer enums when the value represents a closed set owned by the application, for example health states or protocol-specific codes already modeled in the codebase.
- Prefer shared constants when the literal is a repeated structural field name used across classes, for example MDC/log field names such as `requestId`, `httpMethod`, `requestPath`, `httpStatus` and related observability metadata.
- Keep user-facing messages out of business and adapter code. Route them through the localization layer in `org.acme.i18n` and resource bundles under `src/main/resources/i18n`.
- Do not extract every literal mechanically. Keep literals inline when they are local, self-explanatory and do not represent a reusable protocol, domain state, or shared contract.

Enum And Constant Care
----------------------
- Use enums for semantic domains with finite values. Examples: health status, internal protocol error codes, entity states already modeled by the domain.
- Use constants for shared wire-contract field names. This reduces drift between filters, exception mappers, API payload builders and tests.
- Do not replace observability event names blindly. Event values such as `auth.login.failed` or `http.request.completed` behave as log contracts and should only move if there is a clear need for reuse or governance.
- When replacing a string with an enum or constant, keep external behavior unchanged. The refactor is considered correct only if payloads, logs and tests remain semantically identical.
- When in doubt, prefer the most local stable abstraction:
	- framework constant before custom constant
	- existing enum before new enum
	- localization key for user text before static final string in code

Next steps
----------
1. Define domain entities and repository ports in `org.acme.domain` and `org.acme.core`.
2. Implement adapters in `org.acme.adapters.persistence` using Panache entities or standard JPA entities mapped to Postgres.
3. Implement REST controllers in `org.acme.adapters.web` using HTMX-friendly endpoints for server-driven UI.
