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

Next steps
----------
1. Define domain entities and repository ports in `org.acme.domain` and `org.acme.core`.
2. Implement adapters in `org.acme.adapters.persistence` using Panache entities or standard JPA entities mapped to Postgres.
3. Implement REST controllers in `org.acme.adapters.web` using HTMX-friendly endpoints for server-driven UI.
