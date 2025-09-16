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

### Application Flags
- `DEFAULT_AUTH_MODE`: smartphone
    this flag is to ensure that default auth flow for webauthn ceremonies will be smartphone first. 
    It means that user will be prompted to scan QR code with his mobile device on DEFAULT.
    Any other value will result with flow using current user device - for e.g.: on Windows it will be Windows Hello.

### Environment Variables
The application requires the following environment variables to be set for connecting to external services:

-   `SPRING_DATASOURCE_URL`: The JDBC URL for the PostgreSQL database.
-   `SPRING_DATASOURCE_USERNAME`: The username for the database connection.
-   `SPRING_DATASOURCE_PASSWORD`: The password for the database connection.
-   `SPRING_REDIS_HOST`: The hostname of the Redis server.
-   `SPRING_REDIS_PORT`: The port of the Redis server.

## Use Cases Descriptions

For detailed technical documentation on each use case, see the corresponding page in the wiki:

- [Use Case: Initiate Verification](../../wiki/Use-Case-Initiate-Verification)
- [Use Case: Complete Verification](../../wiki/Use-Case-Complete-Verification)
- [Use Case: Initiate Authentication](../../wiki/Use-Case-Initiate-Authentication)
- [Use Case: Complete Authentication](../../wiki/Use-Case-Complete-Authentication)
- [Use Case: Complete Registration](../../wiki/Use-Case-Complete-Registration)

## Implementation Details

This project is built on a modern Spring Boot stack, emphasizing clean architecture and testability. Our core architectural philosophy is based on the **Hexagonal Architecture (Ports and Adapters)** pattern, which isolates the core business logic from external infrastructure concerns like databases, message brokers, and web frameworks.

Key technologies and patterns include:

-   **Hexagonal Architecture**: Business logic is defined in the core application layer, communicating with the outside world through `ports` (interfaces). Infrastructure-specific components are implemented as `adapters`.
-   **Domain vs. Persistence Models**: A strict separation is maintained between pure domain models (e.g., `Passkey`) and their corresponding JPA persistence entities (e.g., `JpaPasskey`). This is enforced via dedicated `Mapper` classes, ensuring the business logic remains entirely independent of the database schema.
-   **Spring Boot 3 & Virtual Threads**: The application is configured to use virtual threads (`spring.threads.virtual.enabled: true`) for improved scalability and throughput under high I/O loads.
-   **PostgreSQL with Spring Data JPA**: Used as the primary relational data store for persistent entities like users and passkey credentials.
- **Redis for State Management**: Used for high-performance, temporary storage of data like authentication `Sessions`,
  leveraging its native TTL (Time-To-Live) feature for automatic expiration.
-   **Type-Safe Configuration**: Uses `@ConfigurationProperties` to bind settings from `application.yaml` to immutable Java records, ensuring configuration is robust and easy to manage.
 
For a comprehensive technical breakdown of specific features, design decisions, and class responsibilities, please refer to our **[Implementation Details Wiki Page](https://github.com/BankApp-project/auth/wiki/Implementation-Details)**.

## Troubleshooting

### Login Issues for Registered Users

**Problem**: Users may experience UX difficulties when attempting to log in after registration.

**Solution**: If login issues occur, implement credential-based authentication by:

1. **Add `allowCredentials` list** to `PublicKeyCredentialRequestOptions` containing the user's registered credentials
2. **Update `Session` object** in `CompleteVerificationUseCase` to include the user's credential data
3. **Modify `InitiateAuthenticationUseCase`** to:
    - Fetch user data and their associated credential list
    - Pass this information to the `Session` object

**Implementation Notes**:

- The `Session` DTO already contains a `credentialId` field of type `List<UUID>` to support this functionality
- This approach ensures WebAuthn ceremonies can reference specific user credentials, improving authentication
  reliability

### Security Considerations for WebAuthn Registration Manager

**Problem**: The current implementation uses `WebAuthnRegistrationManager.createNonStrictWebAuthnRegistrationManager()`
in `PasskeyRegistrationHandler.java:18` which bypasses attestation verification.

**Security Risks**:

- **No Attestation Verification**: The non-strict manager doesn't verify the authenticity or trustworthiness of
  authenticators
- **Reduced Security Assurance**: Cannot validate the provenance of authenticators or detect potentially compromised
  devices
- **Missing Enterprise-Grade Validation**: Lacks verification of attestation certificates and certificate chains
- **Potential Attack Vector**: Malicious or weakened authenticators could be accepted without detection

**Production Security Improvement**:

For production environments requiring enhanced security, consider replacing the non-strict manager with a properly
configured `WebAuthnRegistrationManager` bean that includes:

- Explicit attestation statement verifiers for your target authenticator types
- Certificate path validators with appropriate trust anchors
- Self-attestation trustworthiness verification
- Certificate chain validation

**Research Required**:

- Analyze your target authenticator ecosystem (FIDO2, platform authenticators, etc.)
- Determine appropriate attestation statement verifiers for your use case
- Configure trust anchors and certificate validation policies
- Evaluate attestation requirements based on security policies

**When to Use Strict Verification**:

- Enterprise applications requiring authenticator attestation
- High-security environments (banking, healthcare, government)
- Applications needing to restrict specific authenticator models
- Compliance requirements mandating attestation verification

**References**:

- [W3C WebAuthn Attestation Security](https://www.w3.org/TR/webauthn-1/#sctn-no-attestation-security-attestation)
- [WebAuthn4J Documentation](https://webauthn4j.github.io/webauthn4j/en/)
