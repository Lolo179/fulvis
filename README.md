# Fulvis

Fulvis is a learning project for SDD, hexagonal architecture and agent supervision.

## Requirements

```text
Java 21
Maven 3.6.3 or later
Docker for local PostgreSQL
```

## Commands

```text
mvn test
mvn verify
```

## Local Database

```text
docker compose up -d postgres
```

The default application configuration expects:

```text
FULVIS_DB_URL=jdbc:postgresql://localhost:5432/fulvis
FULVIS_DB_USERNAME=fulvis
FULVIS_DB_PASSWORD=fulvis
```

## Architecture

The scaffold follows a minimal hexagonal structure:

```text
domain
application
adapters
  in
    rest
  out
    persistence
configuration
```

The scaffold intentionally does not implement business behavior. Domain, application, REST and persistence behavior belong to later agent tasks.
