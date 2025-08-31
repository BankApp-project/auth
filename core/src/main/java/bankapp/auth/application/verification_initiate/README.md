# `InitiateVerificationUseCase`


This use case is responsible for initiating the email-based verification flow. It is triggered when a user provides their email address, typically during new user registration or when logging in from an unrecognized device. Its primary function is to orchestrate the generation and delivery of a secure One-Time Password (OTP).

## Process Flow

The `handle` method orchestrates the verification initiation process through two high-level steps:

1.  **Delegate OTP Creation to the Domain**: It calls the `OtpService` to handle the entire lifecycle of creating and persisting the verification data. This service is the single source of truth for OTP business logic and performs the following actions:
    *   **Generate Secure Code**: It calls the `OtpGenerationPort` to create a cryptographically secure, raw OTP code.
    *   **Hash for Storage**: It uses the `HashingPort` to securely hash the raw OTP code. This is a critical security measure to ensure the raw token is never stored.
    *   **Create OTP Object**: It constructs a domain `Otp` object containing the user's email, the hashed code, and a calculated expiration time based on configuration from the `OtpConfigPort`.
    *   **Persist Hashed OTP**: It saves the complete `Otp` object to the persistence layer via the `OtpRepository` port.
    *   Finally, it returns the original **raw** OTP code to the use case for delivery.

2.  **Notify User**: The use case takes the raw OTP code returned by the `OtpService` and calls the `NotificationPort` to send it to the user's provided email address. This is the code the user will need to enter in the application to complete the verification.

### **Dependencies**

This use case relies on the following abstractions to perform its function. The concrete adapters that implement the ports are detailed in the main technical documentation.

*   **Domain Services**:
    *   `OtpService`: A core domain service that encapsulates all business logic for creating, hashing, and persisting the OTP. It acts as a facade for the more granular domain operations.

*   **Ports**:
    *   `NotificationPort`: An outgoing port responsible for dispatching the raw OTP code to the user's email address.
        *   [**View Technical Implementation Details**](https://github.com/BankApp-project/auth/wiki/Implementation-Details#4-feature-asynchronous-notifications-rabbitmq)
