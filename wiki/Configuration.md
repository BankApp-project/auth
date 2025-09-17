# Configuration Guide

This document provides comprehensive configuration instructions for the BankApp Authentication Service, including
environment variables, application properties, and deployment configurations.

## Table of Contents

1. [Environment Variables](#environment-variables)
2. [Application Properties](#application-properties)
3. [Security Configuration](#security-configuration)
4. [Database Configuration](#database-configuration)
5. [Cache Configuration](#cache-configuration)
6. [Messaging Configuration](#messaging-configuration)

---

## Environment Variables

### Core Service Configuration

The service uses environment variables for sensitive and environment-specific configurations. These can be provided
through:

- Direct environment variables
- A `.env` file (for local development)
- Container orchestration secrets (Kubernetes, Docker Swarm)

### Required Variables

| Variable                     | Description                    | Example                                         | Notes                              |
|------------------------------|--------------------------------|-------------------------------------------------|------------------------------------|
| `SPRING_DATASOURCE_URL`      | PostgreSQL connection URL      | `jdbc:postgresql://localhost:5432/bankapp_auth` | Must include database name         |
| `SPRING_DATASOURCE_USERNAME` | Database username              | `bankapp`                                       | Requires appropriate permissions   |
| `SPRING_DATASOURCE_PASSWORD` | Database password              | `secure_password`                               | Use strong passwords in production |
| `SPRING_REDIS_HOST`          | Redis server hostname          | `localhost` or `redis.example.com`              | Can be DNS name or IP              |
| `SPRING_REDIS_PORT`          | Redis server port              | `6379`                                          | Default Redis port is 6379         |
| `RSA_PUBLIC_KEY`             | Base64-encoded RSA public key  | `MIIBIjANBgkqhkiG9w0BAQEF...`                   | For JWT verification               |
| `RSA_PRIVATE_KEY`            | Base64-encoded RSA private key | `MIIEvQIBADANBgkqhkiG9w0B...`                   | For JWT signing                    |

### Optional Variables

| Variable                 | Default      | Description              | Values                 |
|--------------------------|--------------|--------------------------|------------------------|
| `DEFAULT_AUTH_MODE`      | `smartphone` | WebAuthn flow preference | `smartphone`, `device` |
| `SERVER_PORT`            | `8080`       | Application HTTP port    | Any valid port number  |
| `SPRING_PROFILES_ACTIVE` | `default`    | Active Spring profile    | `dev`, `test`, `prod`  |

### Setting Up Environment Variables

#### Local Development (.env file)

Create an `infrastructure/.env` file (already in .gitignore):

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bankapp_auth
SPRING_DATASOURCE_USERNAME=bankapp
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# RSA Keys for JWT
RSA_PUBLIC_KEY=your_base64_encoded_public_key
RSA_PRIVATE_KEY=your_base64_encoded_private_key
```

#### Generating RSA Keys

To generate RSA key pairs for JWT signing:

```bash
# Generate private key
openssl genrsa -out private.pem 2048

# Generate public key
openssl rsa -in private.pem -pubout -out public.pem

# Convert to Base64 (for environment variables)
cat private.pem | base64 -w 0 > private_base64.txt
cat public.pem | base64 -w 0 > public_base64.txt
```

---

## Application Properties

The main configuration file is `application.yaml`, which defines the application's behavior and integrations.

---

## Security Configuration

### JWT Configuration

The service uses RSA key pairs for JWT signing and verification:

- **RSA Key Properties**: Configured through the `RSAProperties` configuration class
- **Key Format**: Base64-encoded RSA keys (PKCS#8 format for private, X.509 for public)
- **Key Validation**: Keys are validated at startup; invalid keys cause application startup failure
- **Token Expiration**: Configurable via application properties

### OAuth2 Authorization Server

The service integrates with Spring Security's OAuth2 Resource Server for token validation:

- **Dependency**: `spring-boot-starter-oauth2-authorization-server`
- **Configuration**: Automatic JWT validation using configured RSA keys
- **Security Filter Chain**: Configured in `SecurityConfiguration.java`

### WebAuthn Security

- **Development Mode**: Uses non-strict `WebAuthnRegistrationManager` for easier testing
- **Production Mode**: Should use strict verification with:
    - Attestation statement verifiers
    - Certificate path validators
    - Trust anchor configuration

---

## Database Configuration

### PostgreSQL Setup

1. Create database and user:

```sql
CREATE DATABASE bankapp_auth;
CREATE USER bankapp WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE bankapp_auth TO bankapp;
```

2. Configure connection pooling (in `application.yaml`):

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### Flyway Migration

Database versioning is managed by Flyway:

- Migration files location: `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
- Auto-migration on startup: Enabled by default

---

## Cache Configuration

### Redis Setup

Redis is used for:

- OTP storage with TTL
- Session management
- WebAuthn challenge caching

Configuration options:

```yaml
spring:
  data:
    redis:
      host: ${SPRING_REDIS_HOST}
      port: ${SPRING_REDIS_PORT}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

---

## Messaging Configuration

### RabbitMQ Setup

For asynchronous notifications:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

app:
  amqp:
    publisher:
      notifications:
        otp:
          exchange: notifications.commands.v1.exchange
          routing-key: send.otp.email
```

---

## Deployment Configurations

### Development Profile

```yaml
spring:
  profiles: dev
  jpa:
    show-sql: true
  logging:
    level:
      bankapp.auth: DEBUG
```

### Production Profile

```yaml
spring:
  profiles: prod
  jpa:
    show-sql: false
  logging:
    level:
      bankapp.auth: INFO
```

### Docker Environment

When deploying with Docker, use environment variable injection:

```dockerfile
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/bankapp_auth
ENV SPRING_REDIS_HOST=redis
ENV RSA_PUBLIC_KEY=${RSA_PUBLIC_KEY}
ENV RSA_PRIVATE_KEY=${RSA_PRIVATE_KEY}
```

---

## Troubleshooting

### Common Configuration Issues

1. **RSA Key Errors**
    - Ensure keys are properly Base64-encoded
    - Check for line breaks in environment variables
    - Validate key pair compatibility

2. **Database Connection Issues**
    - Verify PostgreSQL is running
    - Check network connectivity
    - Confirm user permissions

3. **Redis Connection Issues**
    - Ensure Redis server is accessible
    - Check firewall rules
    - Verify Redis authentication if enabled

4. **Missing Environment Variables**
    - Application fails to start with `InvalidConfigurationPropertiesException`
    - Check all required variables are set
    - Verify `.env` file location for local development

---

*For additional support, refer to the [Implementation Details](Implementation-Details.md) or open an issue in the
repository.*