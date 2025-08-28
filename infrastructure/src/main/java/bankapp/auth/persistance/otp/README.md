# OTP Persistence - Redis Implementation

This package contains the Redis-based implementation for the `OtpRepository` port. It is responsible for the storage and retrieval of One-Time Passwords (OTPs).

## Core Design & Separation of Concerns

This implementation follows a strict separation of concerns between business logic and infrastructure details.

-   **Validation Logic**: The authoritative check for whether an OTP is expired **is performed in the use case layer**, not here. The use case retrieves the `Otp` object and explicitly checks its internal timestamp.

-   **Redis TTL's Role**: This repository uses Redis's native **TTL (Time-To-Live)** feature purely for **housekeeping and garbage collection**. By setting a TTL on each key, we ensure that stale OTPs are automatically purged from the database, preventing it from growing indefinitely. **The TTL is a performance/maintenance feature, not the primary validation mechanism.**

## Key Components

-   **`RedisOtpRepository.java`**: The adapter that implements the `OtpRepository` port. It serializes `Otp` objects to JSON and persists them, applying a TTL on each `save` operation for automatic cleanup.

-   **`OtpConfiguration.java`**: Implements the `OtpConfigPort` interface, providing the repository with the configured TTL duration and a `Clock`.

-   **`OtpProperties.java`**: A type-safe `record` that binds to the `app.config.otp` properties in `application.yaml`.

-   **`OtpMixin`**: A Jackson Mixin to enable deserialization of the immutable `Otp` class.
