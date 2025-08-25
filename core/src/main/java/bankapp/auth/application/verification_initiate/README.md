# `InitiateVerificationUseCase`

This use case is responsible for initiating the email-based verification flow. It is triggered when a user provides their email address, typically during new user registration or when logging in from an unrecognized device. Its primary function is to generate a secure One-Time Password (OTP), dispatch it to the user, and prepare the system for the subsequent verification step.

## Process Flow

The `handle` method orchestrates the verification initiation process through the following steps:

1.  **Generate Secure OTP Data**: It delegates the core logic to the `OtpService`. This service:
    *   Generates a cryptographically secure, raw OTP code of a configured length.
    *   Securely hashes the raw OTP code for storage.
    *   Calculates an expiration time based on a configured Time-To-Live (TTL).
    *   Returns both the raw OTP (for the user) and the hashed OTP object (for persistence).

2.  **Persist Hashed OTP**: The use case takes the hashed OTP object and saves it to the `OtpRepository`. Storing only the hash, along with its expiration time, is a critical security measure that prevents direct token theft from the database.

3.  **Notify User**: It calls the `NotificationPort` to send the original, **raw** OTP code to the user's provided email address. This is the code the user will need to enter in the application to prove ownership of the email account.

4.  **Publish Event**: Upon successful persistence and notification, it publishes an `EmailVerificationOtpGeneratedEvent` via the `EventPublisherPort`. This allows other decoupled parts of the system (e.g., auditing, analytics) to react to the event without creating tight coupling with the verification flow.
