# Notification Integration

## Quick Start

**Want to get email notifications working immediately?** The auth service repository includes a bundled
notification-service configuration for quick testing. Follow these steps:

### 1. Create a Resend Account

1. Sign up for a free account at [Resend.com](https://resend.com)
2. Verify your domain by following Resend's domain verification guidelines
3. Generate an API key from the Resend dashboard
4. Note your verified sender email address (must match your verified domain)

### 2. Configure the Notification Service

```bash
# Navigate to the docker directory
cd docker

# Copy the example configuration
cp .env.notification-service.example .env.docker.notification-service

# Edit the configuration file
# Add your Resend API key and sender email address
```

**Edit `./docker/.env.docker.notification-service`:**

```bash
# Spring Application Configuration
SPRING_APPLICATION_NAME=notification-system
SPRING_DOCKER_COMPOSE_ENABLED=false

# RabbitMQ Configuration
RABBIT_MQ_USER=guest
RABBIT_MQ_PASSWORD=guest
RABBIT_MQ_HOST=rabbitmq
RABBIT_MQ_PORT=5672

# Application-specific Configuration
APP_SEND_TEST_EMAIL=false

# Email Configuration
# ADD YOUR RESEND.COM API KEY
RESEND_API_KEY=re_YOUR_API_KEY_HERE

# ADD YOUR EMAIL ADDRESS FOR SENDING OUT EMAILS HERE
# IMPORTANT: Must match your verified domain in Resend
SENDER_EMAIL_ADDRESS=noreply@yourdomain.com

# Logging Configuration
LOGGING_LEVEL_BANKAPP=DEBUG
```

### 3. Enable the Notification Service

Uncomment the `notification-service` section in `compose.yml`:

```yaml
# notification microservice for straightforward testing.
notification-service:
  image: bankappproject/bankapp-notification-service
  container_name: auth-bankapp-notification-service
  networks:
    - bankapp-auth
  env_file:
    - "docker/.env.docker.notification-service"
  depends_on:
    rabbitmq:
      condition: service_healthy
    auth-service:
      condition: service_healthy
  pull_policy: always
```

### 4. Start the Services

```bash
# Start all services (or just the notification-service)
docker compose up -d

# Or start only the notification service
docker compose up notification-service
```

### 5. Test It

Use the Swagger UI at `http://localhost:8080/api/` to trigger a test notification via the `/verification/complete/email`
endpoint.

---

**That's it!** For production deployments, custom integrations, or more details, continue reading below.

---

## Overview

The auth service publishes notification events to a RabbitMQ exchange using a decoupled, event-driven architecture.
Currently, the service publishes **OTP (One-Time Password) email notifications** when users request authentication
codes.

**Publisher-Consumer Model:**

- **Auth Service** = Publisher (produces notification events)
- **Your Integration** = Consumer (processes and delivers notifications)

The auth service does not handle the actual delivery of emails. It publishes structured messages to RabbitMQ, and you
are responsible for implementing or deploying a consumer service that processes these messages and sends emails.

---

## Message Contract Specification

### Exchange and Routing Configuration

The auth service publishes to a configurable RabbitMQ exchange using topic routing. Default configuration:

| Parameter     | Default Value                        | Environment Variable            |
|---------------|--------------------------------------|---------------------------------|
| Exchange Name | `notifications.commands.v1.exchange` | `NOTIFICATIONS_OTP_EXCHANGE`    |
| Routing Key   | `send.otp.email`                     | `NOTIFICATIONS_OTP_ROUTING_KEY` |
| Exchange Type | `topic`                              | N/A (hardcoded)                 |

**Configuration Location:**

- **Docker deployments**: Edit `./docker/.env.docker` before running `docker compose up -d`
- **Local/production deployments**: Configure via `.env` file in the project root

### Message Payload Structure

All OTP notification messages conform to the `EmailNotificationPayload` schema, distributed via the BankApp shared
payloads library.

**Payload Schema:**

```json
{
  "recipientEmail": "user@example.com",
  "subject": "Your OTP Code",
  "htmlBody": "<html>...</html>"
}
```

**Field Specifications:**

| Field            | Type   | Required | Validation         | Description                                       |
|------------------|--------|----------|--------------------|---------------------------------------------------|
| `recipientEmail` | String | Yes      | Valid email format | Destination email address                         |
| `subject`        | String | Yes      | Non-empty string   | Email subject line                                |
| `htmlBody`       | String | Yes      | Non-empty string   | HTML-formatted email body containing the OTP code |

**Important Notes:**

- All fields are **mandatory**
- The `htmlBody` contains fully-rendered HTML (no template processing required by consumer)
- Messages are serialized as JSON
- Character encoding: UTF-8

### Email Content Customization

**Current Limitation:** The email subject and HTML body are generated by the auth service's `OtpEmailTemplateProvider`
class and are **not currently configurable via environment variables**.

**If you need to customize the email content** (subject line, styling, branding, language, etc.), you will need to:

**Fork and modify the auth service**:

- Edit the `OtpEmailTemplateProvider` class to change the email template
- Rebuild and deploy your customized version
- *(Recommended only if you need deep customization and can maintain a fork)*

**Future Enhancement:** Externalizing email templates to configuration files is planned for a future
release ([#83](https://github.com/BankApp-project/auth-service/issues/83)). If you implement template externalization,
contributions via Pull Request are welcome!

### Using the Shared Payload Library

If you're implementing a Java-based consumer, you can use the official BankApp payloads library for type-safe message
deserialization:

**Maven Configuration:**

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.bankapp-project</groupId>
    <artifactId>bankapp-payloads</artifactId>
    <version>1.0.1</version>
</dependency>
</dependencies>
```

**Java Record:**

```java
package bankapp.payload.notification.email.otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record EmailNotificationPayload(
        @NotEmpty(message = "Email cannot be empty")
        @Email
        String recipientEmail,

        String subject,

        String htmlBody
) {
}
```

---

## Integration Options

You have three approaches for handling notifications published by the auth service. Choose based on your requirements
and infrastructure preferences.

### Option 1: Use BankApp Notification Service (Recommended)

**Best for:** Users who want a ready-to-use solution with minimal setup.

BankApp provides a reference implementation that consumes the notification messages and delivers them via email service
providers.

**Repository:** [BankApp Notification Service](https://github.com/BankApp-project/notification-service)

The notification-service handles:

- Consuming messages from the configured exchange
- Email delivery via supported providers (Resend, etc.)
- Queue management and error handling

**Setup Options:**

- **Quick Testing (Bundled)**: Use the bundled configuration included in the auth-service repository (
  see [Quick Start](#quick-start) above)
- **Production Deployment**: Deploy the notification-service as a separate service by following
  the [notification-service repository's README](https://github.com/BankApp-project/notification-service)

You will only need to configure your email service provider credentials (e.g., Resend API key and verified domain).

---

### Option 2: Implement Your Own Consumer

**Best for:** Users with existing notification infrastructure or specific delivery requirements.

You can implement your own message consumer that processes the `EmailNotificationPayload` messages published by the auth
service.

**Requirements:**

- Implement a RabbitMQ consumer that binds to the configured exchange
- Process messages matching the contract specification (
  see [Message Contract Specification](#message-contract-specification))
- Handle email delivery through your preferred service or SMTP server

**You are responsible for:**

- Queue creation and binding configuration
- Message acknowledgment and error handling
- Retry logic and dead-letter queue management
- Email delivery implementation

---

### Option 3: Switch to REST-Based Notifications

**Best for:** Users who prefer synchronous HTTP communication over message queuing, or have infrastructure constraints
that make RabbitMQ unsuitable.

**⚠️ Warning:** This approach requires modifying the auth service codebase and maintaining a fork.

**What you need to do:**

1. **Implement the `NotificationCommandPublisher` interface** with your REST client logic:

```java
public interface NotificationCommandPublisher {
    void publishSendEmailCommand(EmailNotificationPayload command);
}
```

Your implementation should make an HTTP POST request to your notification service endpoint with the
`EmailNotificationPayload` as the request body.

2. **Configure dependency injection** to use your implementation instead of the default RabbitMQ publisher.

3. **Remove RabbitMQ dependencies** from your project if desired (optional but recommended to reduce unused
   dependencies).

---

## Testing & Verification

### Understanding Message Flow

**Important:** Published notification messages cannot be inspected or consumed without one of the following:

1. **A running notification consumer service** (such as the BankApp notification-service or your custom implementation)
2. **Manual queue creation and binding** in RabbitMQ:
    - Create a queue in the RabbitMQ management UI
    - Bind it to the configured exchange (`NOTIFICATIONS_OTP_EXCHANGE`)
    - Use the configured routing key (`NOTIFICATIONS_OTP_ROUTING_KEY`)

Without either of these in place, messages will be published to the exchange but immediately discarded, as RabbitMQ does
not store messages when no queues are bound to receive them.

### Triggering a Test Notification

To verify that the auth service is publishing notification messages correctly:

1. **Access the Swagger UI** (default location):
   ```
   http://localhost:8080/api/
   ```

2. **Locate the verification endpoint**:
   ```
   POST /verification/complete/email
   ```

3. **Execute a test request** with a valid email address

4. **Verify the outcome**:
    - **If using a notification consumer**: Check that the email was delivered
    - **If using manual queue binding**: Inspect the message in the RabbitMQ management UI queue viewer
    - **If no consumer is configured**: Check RabbitMQ exchange statistics to confirm the message was published (though
      it will not be stored)

### Troubleshooting

If notifications are not working as expected:

- **Check RabbitMQ connection**: Verify the auth service logs for connection errors
- **Verify configuration**: Ensure exchange and routing key match between publisher (auth service) and consumer
- **Confirm queue binding**: In the RabbitMQ management UI, check that your queue is properly bound to the exchange with
  the correct routing key
- **Review consumer logs**: If using a notification service, check its logs for processing errors
