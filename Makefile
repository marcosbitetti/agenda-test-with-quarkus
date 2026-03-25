 # Makefile for common dev tasks (uses Docker Compose)

DOCKER_COMPOSE := docker compose
SERVICE := quarkus-app
WORKDIR := /workspace
MVN := mvn

.PHONY: help docker-up docker-down build test lint coverage clean ci

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
	# Uses maven-checkstyle-plugin if configured in the project; will download plugin if absent
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml checkstyle:check || \
		{ echo "Checkstyle failed or not configured; you can add a checkstyle config to pom.xml"; exit 1; }

coverage:
	# Run tests and generate JaCoCo report under target/site/jacoco
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml test org.jacoco:jacoco-maven-plugin:0.8.8:report
	@echo "Coverage report: quarkus-app/target/site/jacoco/index.html"

clean:
	$(DOCKER_COMPOSE) run --rm -w $(WORKDIR) $(SERVICE) $(MVN) -f pom.xml clean

ci: docker-up lint coverage docker-down
	@echo "CI tasks finished"
