# `CompleteVerificationUseCase`

This use case handles the final step of an email-based verification process, typically initiated after a user has received and submitted a One-Time Password (OTP). Its primary function is to validate the OTP and then prepare for either a new user registration or an existing user login using passkeys.

## Process Flow

The `handle` method orchestrates the verification completion process through the following steps:

1.  **Verify and Consume OTP**: It first validates the provided email and OTP against the stored records. To prevent replay attacks, the OTP is deleted immediately after successful verification. The process will fail if the OTP is expired, invalid, or already used.

2.  **Find or Create User**:
    *   If a user with the given email already exists, their information is retrieved.
    *   If no user is found, a new user account is created and saved to the database.

3.  **Session Management**:
    *   A secure, random challenge and a unique session ID are generated.
    *   A new authentication session is created containing the user ID, challenge, and a configured Time-To-Live (TTL), then stored.

4.  **Prepare Response**: Based on the user's status, it prepares the appropriate response:
    *   **For New Users** (or users without credentials): It generates options for creating a new passkey, returning a `RegistrationResponse`.
    *   **For Existing Users** (who are enabled and have credentials): It generates options for a passkey login request, returning a `LoginResponse`.

## Dependencies

This use case relies on a set of interfaces (Ports) and repositories to interact with external systems and data stores:

*   **Ports**:
    *   `LoggerPort`: For logging information and errors.
    *   `CredentialOptionsPort`: To generate passkey creation or request options.
    *   `ChallengeGenerationPort`: For creating secure challenges.
    *   `HashingPort`: To securely verify the OTP.
*   **Repositories**:
    *   `OtpRepository`: Manages the storage and retrieval of OTPs.
    *   `SessionRepository`: Manages authentication session data.
    *   `UserRepository`: Handles user data persistence.
    *   `CredentialRepository`: Manages user's passkey credentials.
*   **Other**:
    *   `Clock`: Provides the current time to check for OTP and session expiration.
    *   `sessionTtl`: A configuration parameter specifying the duration for which a session is valid.