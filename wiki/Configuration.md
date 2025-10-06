# Configuration Guide

This document provides comprehensive configuration instructions for the BankApp Authentication Service, including
environment variables, application properties, and deployment configurations.

## Table of Contents

1. [Overview](#overview)
2. [Environment Variables](#environment-variables)
    - [Server Configuration](#server-configuration)
    - [Spring Configuration](#spring-configuration)
    - [Database (PostgreSQL) Configuration](#database-postgresql-configuration)
    - [Database Connection Pool (Hikari) Configuration](#database-connection-pool-hikari-configuration)
    - [Database Hibernate Configuration](#database-hibernate-configuration)
    - [Messaging (RabbitMQ) Configuration](#messaging-rabbitmq-configuration)
    - [Caching (Redis) Configuration](#caching-redis-configuration)
    - [Application Specific Configuration](#application-specific-configuration)
    - [Feature Flags](#feature-flags)
    - [Logging Configuration](#logging-configuration)
    - [Documentation (Swagger/OpenAPI) Configuration](#documentation-swaggeropenapi-configuration)
3. [Configuration Files](#configuration-files)
4. [Environment-Specific Configurations](#environment-specific-configurations)

---

## Overview

The BankApp Authentication Service uses a combination of environment variables and YAML configuration files to manage
its settings. This approach provides:

- **Flexibility**: Easy to configure across different environments (dev, test, prod)
- **Security**: Sensitive values (passwords, secrets) are kept in environment variables
- **Type Safety**: Configuration properties are bound to Java records using `@ConfigurationProperties`
- **Validation**: Spring Boot validates configuration at startup

All configuration values can be set via environment variables or in the `.env` file. A complete example is provided in
`.env.example` at the project root.

---

## Environment Variables

### Server Configuration

| Variable         | Description                                                                          | Default   | Example   |
|------------------|--------------------------------------------------------------------------------------|-----------|-----------|
| `SERVER_ADDRESS` | The network address the server will bind to. Use `0.0.0.0` to bind to all interfaces | `0.0.0.0` | `0.0.0.0` |
| `PORT`           | The port the application will listen on                                              | `8080`    | `8080`    |
| `CONTEXT_PATH`   | The base path for all endpoints                                                      | `/api`    | `/api`    |

**Example:**

```bash
SERVER_ADDRESS=0.0.0.0
PORT=8080
CONTEXT_PATH=/api
```

---

### Spring Configuration

| Variable                         | Description                                                      | Default              | Example               |
|----------------------------------|------------------------------------------------------------------|----------------------|-----------------------|
| `SPRING_PROFILES_ACTIVE`         | Set the active Spring profile                                    | `dev`                | `dev`, `test`, `prod` |
| `SPRING_THREADS_VIRTUAL_ENABLED` | Enable or disable Java 21+ Virtual Threads for handling requests | `true`               | `true`, `false`       |
| `SPRING_DOCKER_COMPOSE_FILE`     | Path to the docker compose file for integration testing          | `./compose-test.yml` | `./compose-test.yml`  |

**Example:**

```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_THREADS_VIRTUAL_ENABLED=true
SPRING_DOCKER_COMPOSE_FILE=./compose-test.yml
```

**Notes:**

- **Virtual Threads**: Requires Java 21 or later. Dramatically improves throughput for I/O-bound operations
- **Spring Profiles**: Used to activate profile-specific configuration in `application.yaml`

---

### Database (PostgreSQL) Configuration

| Variable            | Description                | Default        | Example                                |
|---------------------|----------------------------|----------------|----------------------------------------|
| `POSTGRES_HOST`     | PostgreSQL server hostname | `localhost`    | `localhost`, `postgres-db.example.com` |
| `POSTGRES_PORT`     | PostgreSQL server port     | `5432`         | `5432`                                 |
| `POSTGRES_DB`       | Database name              | `auth-service` | `auth-service`                         |
| `POSTGRES_USER`     | Database username          | `dev`          | `dev`, `prod_user`                     |
| `POSTGRES_PASSWORD` | Database password          | `dev`          | `dev`, `secure_password_123`           |

**Example:**

```bash
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=auth-service
POSTGRES_USERNAME=dev
POSTGRES_PASSWORD=dev
```

**Security Notes:**

- **Never commit passwords to version control**
- Use strong passwords in production environments
- Consider using connection pooling for better performance

---

### Database Connection Pool (Hikari) Configuration

| Variable                    | Description                                                     | Default  | Example            |
|-----------------------------|-----------------------------------------------------------------|----------|--------------------|
| `HIKARI_MAX_POOL_SIZE`      | Maximum number of connections in the pool                       | `5`      | `5`, `10`, `20`    |
| `HIKARI_MIN_IDLE`           | Minimum number of idle connections to maintain                  | `2`      | `2`, `5`           |
| `HIKARI_CONNECTION_TIMEOUT` | Maximum time (ms) to wait for a connection from the pool        | `20000`  | `20000`, `30000`   |
| `HIKARI_IDLE_TIMEOUT`       | Maximum time (ms) a connection can be idle before being retired | `300000` | `300000`, `600000` |

**Example:**

```bash
HIKARI_MAX_POOL_SIZE=5
HIKARI_MIN_IDLE=2
HIKARI_CONNECTION_TIMEOUT=20000
HIKARI_IDLE_TIMEOUT=300000
```

**Performance Tuning:**

- **Max Pool Size**: Set based on expected concurrent database operations
- **Min Idle**: Keep enough connections warm to handle typical load
- **Connection Timeout**: Balance between user experience and resource usage

---

### Database Hibernate Configuration

| Variable             | Description                             | Default | Example         |
|----------------------|-----------------------------------------|---------|-----------------|
| `HIBERNATE_SHOW_SQL` | Enable or disable SQL statement logging | `false` | `true`, `false` |

**Example:**

```bash
HIBERNATE_SHOW_SQL=false
```

**Notes:**

- Set to `true` in development to debug SQL queries
- **Always set to `false` in production** to avoid performance overhead and potential security issues
- Externalizes the `spring.jpa.show-sql` property for easier environment-specific configuration

---

### Messaging (RabbitMQ) Configuration

| Variable             | Description              | Default     | Example                             |
|----------------------|--------------------------|-------------|-------------------------------------|
| `RABBIT_MQ_HOST`     | RabbitMQ server hostname | `localhost` | `localhost`, `rabbitmq.example.com` |
| `RABBIT_MQ_PORT`     | RabbitMQ server port     | `5672`      | `5672`                              |
| `RABBIT_MQ_USERNAME` | RabbitMQ username        | `guest`     | `guest`, `prod_user`                |
| `RABBIT_MQ_PASSWORD` | RabbitMQ password        | `guest`     | `guest`, `secure_password_123`      |

**Example:**

```bash
RABBIT_MQ_HOST=localhost
RABBIT_MQ_PORT=5672
RABBIT_MQ_USERNAME=guest
RABBIT_MQ_PASSWORD=guest
```

**Integration Notes:**

- Used for asynchronous notification delivery (email OTPs)
- The application publishes notification commands to exchanges
- A separate notification service consumes these messages
-
See [Implementation Details - Asynchronous Notifications](Implementation-Details.md#4-feature-asynchronous-notifications-rabbitmq)
for architecture details

---

### Caching (Redis) Configuration

| Variable         | Description               | Default     | Example                          |
|------------------|---------------------------|-------------|----------------------------------|
| `REDIS_HOST`     | Redis server hostname     | `localhost` | `localhost`, `redis.example.com` |
| `REDIS_PORT`     | Redis server port         | `6379`      | `6379`                           |
| `REDIS_USERNAME` | Redis username (optional) | _(empty)_   | `redis_user`                     |
| `REDIS_PASSWORD` | Redis password (optional) | _(empty)_   | `secure_password_123`            |

**Example:**

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=
REDIS_PASSWORD=
```

**Usage:**

- Stores OTP hashes with automatic expiration (TTL-based cleanup)
- Caches WebAuthn session data (challenges, ceremony state)
- Provides sub-second access times for authentication flows

---

### Application Specific Configuration

#### CORS Configuration

| Variable                     | Description                                                               | Default                 | Example                                         |
|------------------------------|---------------------------------------------------------------------------|-------------------------|-------------------------------------------------|
| `APP_CORS_ALLOWED_ORIGINS`   | Comma-separated list of allowed origins for Cross-Origin Resource Sharing | `http://auth.localhost` | `http://localhost:3000,https://app.example.com` |
| `APP_CORS_ALLOW_CREDENTIALS` | Whether to allow credentials (e.g., cookies, authorization headers)       | `true`                  | `true`, `false`                                 |

**Example:**

```bash
APP_CORS_ALLOWED_ORIGINS=http://auth.localhost
APP_CORS_ALLOW_CREDENTIALS=true
```

**Security Notes:**

- Only add trusted frontend origins
- Be cautious with `*` (wildcard) - avoid in production
- When `allowCredentials=true`, you cannot use `*` for origins

#### Passkey/WebAuthn Configuration

| Variable             | Description                                                            | Default            | Example                                   |
|----------------------|------------------------------------------------------------------------|--------------------|-------------------------------------------|
| `APP_PASSKEY_RPID`   | Relying Party ID. Should be the domain name of your application        | `localhost`        | `localhost`, `example.com`                |
| `APP_PASSKEY_ORIGIN` | Relying Party Origin. The full origin URL of your frontend application | `http://localhost` | `http://localhost`, `https://example.com` |
| `APP_AUTH_MODE`      | Authentication mode for passkeys                                       | `smartphone`       | `smartphone`, `standard`                  |
| `APP_UP_FLAG`        | Require user presence (e.g., touch ID, button press)                   | `true`             | `true`, `false`                           |
| `APP_UV_FLAG`        | Require user verification (e.g., PIN, biometric)                       | `true`             | `true`, `false`                           |

**Example:**

```bash
APP_PASSKEY_RPID=localhost
APP_PASSKEY_ORIGIN=http://localhost
APP_AUTH_MODE=smartphone
APP_UP_FLAG=true
APP_UV_FLAG=true
```

**Configuration Notes:**

- **RPID**: Must match the domain of your frontend (without protocol/port)
- **Origin**: Must exactly match the URL where your frontend is served
- **Auth Mode**:
    - `smartphone`: Cross-device authentication (QR code flow)
    - `standard`: Platform authenticators preferred (built-in fingerprint, Face ID)
- **UP Flag**: User Presence verification (lower security, broader compatibility)
- **UV Flag**: User Verification (higher security, may exclude some authenticators)

#### OTP (One-Time Password) Configuration

| Variable       | Description                                | Default | Example                 |
|----------------|--------------------------------------------|---------|-------------------------|
| `APP_OTP_SIZE` | The number of digits for the generated OTP | `6`     | `6`, `8`                |
| `APP_OTP_TTL`  | Time-to-live for an OTP                    | `5m`    | `60s`, `2m`, `5m`, `1h` |

**Example:**

```bash
APP_OTP_SIZE=6
APP_OTP_TTL=5m
```

**Best Practices:**

- **Size**: 6 digits is standard and user-friendly
- **TTL**: Balance security (shorter is better) with user experience (longer is more convenient)

#### Challenge Configuration

| Variable                  | Description                                                      | Default | Example               |
|---------------------------|------------------------------------------------------------------|---------|-----------------------|
| `APP_CHALLENGE_LEN_BYTES` | The length in bytes for security challenges (e.g., for WebAuthn) | `32`    | `16`, `32`, `64`      |
| `APP_CHALLENGE_TTL`       | Time-to-live for a challenge                                     | `120s`  | `60s`, `120s`, `300s` |

**Example:**

```bash
APP_CHALLENGE_LEN_BYTES=32
APP_CHALLENGE_TTL=120s
```

**Security Considerations:**

- **Length**: 32 bytes (256 bits) provides strong cryptographic security
- **TTL**: Short enough to prevent replay attacks, long enough for typical user flows

---

### Feature Flags

| Variable                                      | Description                                   | Default | Example         |
|-----------------------------------------------|-----------------------------------------------|---------|-----------------|
| `APP_FEATURE_VERIFICATION_INITIATE_ENABLED`   | Enable email verification initiation endpoint | `true`  | `true`, `false` |
| `APP_FEATURE_VERIFICATION_COMPLETE_ENABLED`   | Enable email verification completion endpoint | `true`  | `true`, `false` |
| `APP_FEATURE_AUTHENTICATION_INITIATE_ENABLED` | Enable authentication initiation endpoint     | `true`  | `true`, `false` |
| `APP_FEATURE_AUTHENTICATION_COMPLETE_ENABLED` | Enable authentication completion endpoint     | `true`  | `true`, `false` |
| `APP_FEATURE_REGISTRATION_COMPLETE_ENABLED`   | Enable registration completion endpoint       | `true`  | `true`, `false` |

**Example:**

```bash
APP_FEATURE_VERIFICATION_INITIATE_ENABLED=true
APP_FEATURE_VERIFICATION_COMPLETE_ENABLED=true
APP_FEATURE_AUTHENTICATION_INITIATE_ENABLED=true
APP_FEATURE_AUTHENTICATION_COMPLETE_ENABLED=true
APP_FEATURE_REGISTRATION_COMPLETE_ENABLED=true
```

**Usage:**

- Allows selective enabling/disabling of API endpoints
- Useful for gradual rollouts or maintenance windows
- Disabled endpoints return `404 Not Found` to clients

---

### Logging Configuration

| Variable                    | Description                                                   | Default | Example                                   |
|-----------------------------|---------------------------------------------------------------|---------|-------------------------------------------|
| `LOGGING_LEVEL_SPRING_BOOT` | Logging level for Spring Boot's auto-configuration components | `INFO`  | `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `LOGGING_LEVEL_BANKAPP`     | Logging level for your application's specific packages        | `DEBUG` | `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` |

**Example:**

```bash
LOGGING_LEVEL_SPRING_BOOT=INFO
LOGGING_LEVEL_BANKAPP=DEBUG
```

**Log Level Guidelines:**

- **Production**: Use `INFO` or `WARN` to reduce noise
- **Development**: Use `DEBUG` to see detailed execution flow
- **Troubleshooting**: Temporarily set to `TRACE` for maximum verbosity

**Logging Features:**

- **Structured Logging**: JSON-formatted logs with contextual information
- **Email Masking**: Sensitive data (email addresses) are partially masked in logs
- **Correlation IDs**: Each request is tagged with a unique correlation ID for traceability
- See [Implementation Details - Logging](Implementation-Details.md#7-feature-logging-and-observability) for architecture
  details

---

### Documentation (Swagger/OpenAPI) Configuration

| Variable          | Description                                                                   | Default | Example         |
|-------------------|-------------------------------------------------------------------------------|---------|-----------------|
| `SWAGGER_ON_ROOT` | If true, serves Swagger UI from the root path (/) instead of /swagger-ui.html | `true`  | `true`, `false` |

**Example:**

```bash
SWAGGER_ON_ROOT=true
```

**Access:**

- When `true`: Navigate to `http://localhost:8080/` to view the API documentation
- When `false`: Navigate to `http://localhost:8080/swagger-ui.html`

---

## Configuration Files

### application.yaml

The main configuration file located at `infrastructure/src/main/resources/application.yaml`. This file:

- Defines the structure of configuration properties
- Maps environment variables to Spring Boot properties
- Uses `@ConfigurationProperties` binding for type-safe configuration
- Supports profile-specific overrides (e.g., `application-dev.yaml`, `application-prod.yaml`)

**Example snippet:**

```yaml
server:
  address: ${SERVER_ADDRESS:0.0.0.0}
  port: ${PORT:8080}
  servlet:
    context-path: ${CONTEXT_PATH:/api}

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

### .env File

A local environment file (ignored by Git) where you set your environment-specific values. Copy `.env.example` to `.env`
and customize:

```bash
cp .env.example .env
# Edit .env with your local configuration
```

---

## Environment-Specific Configurations

### Development Environment

```bash
SPRING_PROFILES_ACTIVE=dev
LOGGING_LEVEL_BANKAPP=DEBUG
HIBERNATE_SHOW_SQL=true
```

**Characteristics:**

- Verbose logging for debugging
- SQL queries logged to console
- Relaxed security settings (if applicable)
- May use Docker Compose for local dependencies

### Test Environment

```bash
SPRING_PROFILES_ACTIVE=test
LOGGING_LEVEL_BANKAPP=INFO
HIBERNATE_SHOW_SQL=false
SPRING_DOCKER_COMPOSE_FILE=./compose-test.yml
```

**Characteristics:**

- Moderate logging
- Uses test-specific compose file for isolated testing
- May use embedded databases or test containers

### Production Environment

```bash
SPRING_PROFILES_ACTIVE=prod
LOGGING_LEVEL_BANKAPP=INFO
LOGGING_LEVEL_SPRING_BOOT=WARN
HIBERNATE_SHOW_SQL=false
```

**Characteristics:**

- Minimal logging (INFO/WARN only)
- No SQL query logging
- Strong passwords and secure credentials
- Monitoring enabled (Spring Boot Actuator)
- HTTPS enforced for passkey origins

---

## Additional Resources

- [Implementation Details](Implementation-Details.md) - Deep dive into architectural patterns and configuration usage
- [Environment Variables Best Practices](https://12factor.net/config) - The Twelve-Factor App methodology
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config) -
  Official Spring Boot documentation
