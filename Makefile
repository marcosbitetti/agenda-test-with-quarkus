 # Makefile for common dev tasks (uses Docker Compose)

DOCKER_COMPOSE := docker compose
SERVICE := quarkus-app
WORKDIR := /workspace
MVN := mvn

.PHONY: help docker-up docker-down build test lint lint-fix coverage clean ci

help:
	@echo "Usage: make <target>"
	@echo "Available targets:"
	@echo "  docker-up     - start dependent services (DB, Keycloak) via docker compose"
	@echo "  docker-down   - stop and remove containers"
	@echo "  build         - build the quarkus module (skip tests)"
	@echo "  test          - run Maven tests inside the quarkus container"
	@echo "  lint          - run maven-based lint (checkstyle) inside container"
	@echo "  coverage      - run tests and generate JaCoCo coverage report"
	@echo "  clean         - clean build artifacts"
	@echo "  ci            - start services, run lint+coverage, then stop services"

docker-up:
	$(DOCKER_COMPOSE) up -d

docker-down:
	$(DOCKER_COMPOSE) down --remove-orphans

build:
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -DskipTests -f pom.xml package

test:
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml test

lint:
	@# Run Checkstyle only when the project explicitly configures it.
	@if grep -q "maven-checkstyle-plugin" $(SERVICE)/pom.xml; then \
		$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml checkstyle:check; \
	else \
		echo "Checkstyle plugin is not configured in $(SERVICE)/pom.xml; skipping lint."; \
	fi

# Try to apply automatic formatting/fixes using common Maven plugins.
# This is best-effort: it uses Spotless when configured and otherwise falls back to Revelc formatter.
lint-fix:
	@echo "Attempting to apply automatic formatting (spotless / formatter)..."
	@if grep -q "spotless-maven-plugin" $(SERVICE)/pom.xml; then \
		$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml com.diffplug.spotless:spotless-maven-plugin:apply || \
		$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml net.revelc.code.formatter:formatter-maven-plugin:format; \
	else \
		$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml net.revelc.code.formatter:formatter-maven-plugin:format; \
	fi || { echo "No auto-format plugin applied. Add Spotless or formatter-maven-plugin to pom.xml, then re-run 'make lint-fix'."; exit 0; }

coverage:
	# Run tests and generate JaCoCo report under target/site/jacoco
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml test org.jacoco:jacoco-maven-plugin:0.8.8:report
	@echo "Coverage report: quarkus-app/target/site/jacoco/index.html"

clean:
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml clean

ci: docker-up lint coverage docker-down
	@echo "CI tasks finished"
