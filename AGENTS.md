Welcome to the team, Jules. This document is your primary source of truth for understanding our project's architecture, conventions, and best practices. Adherence to these guidelines is critical for maintaining the quality and consistency of our codebase.

## 1. Project Overview

This project is a backend service for user authentication using a passwordless, passkey-based (WebAuthn) flow.

The core logic is centered around two primary use cases:
1.  `InitiateVerificationUseCase`: Handles the creation and sending of a One-Time Password (OTP) to a user's email.
2.  `CompleteVerificationUseCase`: Verifies the OTP and then prepares the system for either a new passkey registration (for new users) or a passkey-based login (for existing users).

## 2. Core Architecture: Hexagonal (Ports & Adapters)

We strictly follow the Hexagonal Architecture pattern. This is the most important concept to understand.

-   **Domain Layer (The "Hexagon"):** This is the core of our application.
    -   It contains the business logic (`UseCases`), interfaces (`Ports`), and pure domain models (e.g., `User`, `Passkey`).
    -   **CRITICAL RULE:** The domain layer must have **zero** dependencies on any external framework or infrastructure. No Spring annotations, no JPA annotations, no Redis-specific code. It is pure Java.

-   **Infrastructure Layer (The "Adapters"):** This is the outer layer.
    -   It contains the concrete implementations of the `Ports` defined in the domain layer.
    -   This is where all external libraries and frameworks are used (Spring Data JPA, Spring Security, Redis, etc.).
    -   Examples: `PostgresUserRepository` is an adapter that implements the `UserRepository` port.

-   **Application Layer:** The entry point to the system.
    -   This consists of our REST controllers, which directly call the Use Case classes.

## 3. Key Principles, Rules, & Best Practices

-   **Test-Driven Development (TDD) is Mandatory:** Always write a failing test *before* writing the implementation code. Your process should always be: Red -> Green -> Refactor.

-   **Strict Separation of Models:**
    -   **Domain Models:** Pure Java objects representing business concepts (`User`, `Passkey`).
    -   **Persistence Models:** Dedicated classes annotated with `@Entity` for database mapping (`JpaUser`, `CredentialRecord`).
    -   **Mappers:** A dedicated, test-covered mapper class is required to convert between the Domain and Persistence models. This mapping happens inside the infrastructure adapter.

-   **Use of `EmailAddress` Value Object:**
    -   All email addresses within the domain and application layers **must** be represented by the `EmailAddress` Value Object (VO). This ensures type safety, validation, and data integrity throughout the system.
    -   **Data Flow:** It is the responsibility of the adapters (e.g., in the web/controller layer) to convert incoming string-based emails into `EmailAddress` objects before passing them to the use cases.

-   **Prefer `@RequiredArgsConstructor` for Dependency Injection:**
    -   For constructor-based dependency injection, use Lombok's `@RequiredArgsConstructor` on the class instead of writing the constructor manually. This reduces boilerplate code and improves readability.

-   **Dependency Inversion:** The domain layer depends on abstractions (`Ports`), not on concrete implementations (`Adapters`). Spring's Dependency Injection is used to wire the concrete adapters into the use cases at runtime.

-   **Single Responsibility:** Each class should have one clear purpose. Repositories handle data access, mappers handle object conversion, use cases handle business workflows.

## 4. Technology Stack & Key Decisions

| Category              | Technology / Library                 | Rationale & Conventions                                                                                                                                              |
| --------------------- | ------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Language/Framework**| Java / Spring Boot                   | The foundation of our application.                                                                                                                                   |
| **Database**          | PostgreSQL / Spring Data JPA         | Standard relational database. All interactions are through JPA repositories.                                                                                         |
| **Caching**           | Redis / `RedisTemplate`              | Used for storing short-lived data like the `Challenge`.                                                                                                              |
| **Testing**           | JUnit 5, Mockito, **Testcontainers** | **Testing Strategy:** Unit tests use Mockito for isolation. Integration tests for infrastructure adapters (database, cache) **must be written using Testcontainers** to validate against real technology (e.g., PostgreSQL, Redis). **You will not run these tests**, but you are responsible for writing them correctly. The CI pipeline will execute them. |
| **Hashing (OTP)**     | Spring Security / `BCrypt`           | We use the full `spring-boot-starter-security` dependency. **Reason:** It is the idiomatic approach and future-proofs the service for when we need endpoint security. |
| **Challenge Generation**| `java.security.SecureRandom` (32 bytes)| Chosen for its cryptographic security guarantees, which are required by the WebAuthn spec. Preferred over UUIDs, which may have predictable components.             |
| **Passkeys/WebAuthn** | `webauthn4j` (Data Models)           | We use the library's data-transfer objects (e.g., `PublicKeyCredentialCreationOptions`) but implement the core logic ourselves for full control.                   |

## 5. Development Workflow & GitHub Tooling

We use a structured approach for planning and executing work. When you are assigned work, it will be in the following format.

1.  **Feature (`✨ Feature: [Title]`)**:
    -   A high-level, shippable piece of functionality.
    -   Contains user stories, acceptance criteria, and a checklist of implementation tasks.

2.  **Task (`✅ Task: [Title]`)**:
    -   A specific technical implementation of one component, linked to a parent Feature.
    -   Must include a detailed **Acceptance Criteria** checklist that follows the TDD workflow.

## 6. Your Role & Responsibilities

-   **Strict Module Boundaries:** Your work is confined to the `infrastructure` module. You must **never** change any code in the `domain` or `application` modules without explicit prior permission. Your role is to implement adapters based on pre-defined `Port` interfaces, not to define business logic or API contracts.

-   **Code Generation:** Any code you provide must strictly adhere to the Hexagonal Architecture and all principles and rules outlined in this document.

-   **Problem Solving:** When asked for solutions, provide answers that are consistent with our established technology stack and decisions.

-   **TDD Adherence:** Always frame your implementation suggestions and code generation within the Red-Green-Refactor cycle.

-   **Testing Responsibility:** You must write two types of tests: 1) Fast unit tests with Mockito for isolated logic. 2) True integration tests for infrastructure adapters using **Testcontainers**. While you cannot run the Testcontainers tests yourself, you are responsible for writing them correctly. Our CI environment will execute them to guarantee they work with the real database and cache.
