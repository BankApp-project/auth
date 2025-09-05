## Rules

### Use Case's beans
After finishing implementation of all use case's ports, add `@UseCase` annotation on class level of specific use case:
```java
@UseCase //this
public class InitiateVerificationUseCase {
//(...)
}
```

### EmailAddress VO
We use VO `EmailAddress` for all email addresses at whole application level. 
Security and data integrity measures.

**FLOW:**
```
outside world format -> Adapter -> EmailAddress email -> IN -> 
some bussiness logic -> OUT -> outside world format
```

### @RequiredArgsConstructor usage
We use `@RequiredArgsConstructor` in favor of `@Autowired` or writing constructors if it is feasible.

#### GOOD

```java
@RequiredArgsConstructor
public class VerificationController {

    private final InitiateVerificationUseCase initiateVerificationUseCase;
    private final CompleteVerificationUseCase completeVerificationUseCase;
//(...)
}
```

#### BAD
```java
public class VerificationController {

    private final InitiateVerificationUseCase initiateVerificationUseCase;
    private final CompleteVerificationUseCase completeVerificationUseCase;

    public VerificationController(
            @Autowired InitiateVerificationUseCase initiateVerificationUseCase, 
            @Autowired CompleteVerificationUseCase completeVerificationUseCase) {
        this.initiateVerificationUseCase = initiateVerificationUseCase;
        this.completeVerificationUseCase = completeVerificationUseCase;
    }
//(...)
}
```

## Configuration

- `DEFAULT_AUTH_MODE`: smartphone
    this flag is to ensure that default auth flow for webauthn ceremonies will be smartphone first. 
    It means that user will be prompted to scan QR code with his mobile device on DEFAULT.
    Any other value will result with flow using current user device - for e.g.: on Windows it will be Windows Hello.

## Use Cases Descriptions

For detailed technical documentation on each use case, see the corresponding page in the wiki:

- [Use Case: Complete Authentication](../../wiki/Use-Case:-Complete-Authentication)
- [Use Case: Complete Verification](../../wiki/Use-Case:-Complete-Verification)
- [Use Case: Initiate Authentication](../../wiki/Use-Case:-Initiate-Authentication)
- [Use Case: Initiate Verification](../../wiki/Use-Case:-Initiate-Verification)
- [Use Case: Registration Complete](../../wiki/Use-Case:-Registration-Complete)

## Implementation Details

This project is built on a modern Spring Boot stack, emphasizing clean architecture and testability. Our core architectural philosophy is based on the **Hexagonal Architecture (Ports and Adapters)** pattern, which isolates the core business logic from external infrastructure concerns like databases, message brokers, and web frameworks.

Key technologies and patterns include:

-   **Hexagonal Architecture**: Business logic is defined in the core application layer, communicating with the outside world through `ports` (interfaces). Infrastructure-specific components, such as the Redis repository or RabbitMQ publishers, are implemented as `adapters`.
-   **Spring Boot 3 & Virtual Threads**: The application is configured to use virtual threads (`spring.threads.virtual.enabled: true`) for improved scalability and throughput under high I/O loads.
-   **Redis for State Management**: Used for high-performance, temporary storage of data like One-Time Passwords (OTPs), leveraging its native TTL (Time-To-Live) feature for automatic expiration.
-   **RabbitMQ for Asynchronous Communication**: Facilitates decoupled, event-driven workflows, such as sending notifications.
-   **Type-Safe Configuration**: Uses `@ConfigurationProperties` to bind settings from `application.yaml` to immutable Java records, ensuring configuration is robust and easy to manage.

For a comprehensive technical breakdown of specific features, design decisions, and class responsibilities, please refer to our **[Implementation Details Wiki Page](https://github.com/BankApp-project/auth/wiki/Implementation-Details)**.
