This use case handles the final step of the passkey authentication flow for a returning user. After the user has been
prompted by their device to authorize a login (`navigator.credentials.get()`), this class receives the resulting signed
assertion. It performs the cryptographic verification, confirms the user's identity, and issues authentication tokens to
complete the login process.

## Process Flow

The `handle` method orchestrates the authentication completion process through the following steps:

1. **Verify and Update Passkey**: This is the core security step that consolidates several operations:
    * It retrieves the authentication session using the provided session ID to get the challenge.
    * It loads the user's stored `Passkey` credential from the database using the credential ID.
    * It delegates to the `PasskeyVerificationPort` to cryptographically verify the client's signed assertion against
      the stored challenge and the credential's public key.
    * As part of the FIDO2 security protocol, successful verification involves updating the credential's signature
      counter and saving it back to the repository. This is critical for detecting cloned authenticators.
    * **Security Enhancement**: The method now includes special handling for `MaliciousCounterException`, which
      indicates potential credential cloning attacks.

2. **Clean Up Session**: To prevent replay attacks, the authentication session is deleted immediately after successful
   verification.

3. **Issue Authentication Tokens**: Once the user's identity is confirmed, the `TokenIssuingPort` generates a new set of
   authentication tokens (e.g., JWTs) for the verified user.

4. **Return Authentication Grant**: The method returns an `AuthenticationGrant` containing the newly issued tokens,
   which the client can use to access the application's protected resources.

## Dependencies

This use case relies on a set of interfaces (Ports) and repositories to perform its function:

* **Ports**:
    * `PasskeyVerificationPort`: Handles WebAuthn credential registration and authentication verification with signature
      counter validation.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#5-feature-passkey-management-and-authentication-webauthn)
    * `TokenIssuingPort`: Responsible for creating and signing authentication tokens.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#token-issuing-stub-implementation)
* **Repositories**:
    * `SessionRepository`: Manages the storage and retrieval of authentication process related data.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)
    * `PasskeyRepository`: Manages the persistence of user passkey credentials, including loading them for
      verification and saving their updated state.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#user-and-passkey-persistence)
