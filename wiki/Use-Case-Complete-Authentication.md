This use case handles the final step of the passkey authentication flow for a returning user. After the user has been
prompted by their device to authorize a login (`navigator.credentials.get()`), this class receives the resulting signed
assertion. It performs the cryptographic verification, confirms the user's identity, and issues authentication tokens to
complete the login process.

## Process Flow

The `handle` method orchestrates the authentication completion process through the following steps:

1. **Retrieve Challenge**: It loads the authentication challenge from the repository using the session ID provided in
   the command. This ensures the login attempt corresponds to a valid, server-initiated request.

2. **Verify Signature and Update Credential**: This is the core security step.
    * It retrieves the user's stored `Passkey` credential from the database.
    * It delegates to the `WebAuthnPort` to cryptographically verify the client's signed assertion against the stored
      challenge and the credential's public key.
    * As part of the FIDO2 security protocol, a successful verification also involves updating the credential's
      signature counter. The `WebAuthnPort` returns the updated credential object.

3. **Save Updated Credential**: The credential, now with its updated signature counter, is saved back to the
   `CredentialRepository`. This is a critical step to protect against cloned authenticators.

4. **Consume Challenge**: To prevent replay attacks, the challenge is deleted immediately after it has been successfully
   used for authentication.

5. **Issue Authentication Tokens**: Once the user's identity is confirmed, the `TokenIssuingPort` generates a new set of
   authentication tokens (e.g., JWTs) for the user.

6. **Return Authentication Grant**: The method returns an `AuthenticationGrant` containing the newly issued tokens,
   which the client can use to access the application's protected resources.

## Dependencies

This use case relies on a set of interfaces (Ports) and repositories to perform its function:

* **Ports**:
    * `PasskeyVerificationPort`: Handles WebAuthn credential registration and authentication verification with signature
      counter validation.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#passkey-verification)
    * `TokenIssuingPort`: Responsible for creating and signing authentication tokens.
* **Repositories**:
    * `SessionRepository`: Manages the storage and retrieval of authentication process related data.
    * `CredentialRepository`: Manages the persistence of user passkey credentials, including loading them for
      verification and saving their updated state.
