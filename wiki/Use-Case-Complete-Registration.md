This use case handles the final, critical step of the new user registration flow. After a user has verified their email
and been prompted to create a passkey, this class takes the response from their authenticator (
`navigator.credentials.create()`), verifies it, creates the user's first credential, activates their account, and issues
authentication tokens to log them in immediately.

## Process Flow

The `handle` method orchestrates the registration completion process through the following steps:

1. **Retrieve Challenge**: It loads the challenge that was generated and stored during the email verification step. This
   ensures the registration attempt is tied to a valid, active session.

2. **Verify and Extract Credential**: It uses the `WebAuthnPort` to perform the core FIDO2/WebAuthn ceremony. This
   involves cryptographically verifying the client's registration data against the server-side challenge. If
   verification is successful, a `Passkey` object is created, containing the new public key and credential details.

3. **Save New Credential**: The newly verified `Passkey` credential is persisted in the `CredentialRepository`, securely
   associating it with the user's account.

4. **Consume Challenge**: To prevent replay attacks, the challenge is deleted immediately after it has been successfully
   used.

5. **Activate User**: The user's account is fetched, and its status is set to "active" or "enabled." This is a crucial
   step that transitions the user from a pending state to a fully registered member. The updated user status is saved.

6. **Issue Authentication Tokens**: Upon successful activation, the system immediately issues a set of authentication
   tokens (e.g., JWTs) for the user. This provides a seamless user experience by logging them in right after they create
   their account.

7. **Return Authentication Grant**: The method returns an `AuthenticationGrant` containing the newly issued tokens,
   which the client can use to access protected resources.

## Dependencies

This use case relies on a set of interfaces (Ports) and repositories to perform its function:

* **Ports**:
    * `PasskeyVerificationPort`: Handles WebAuthn credential registration and authentication verification with signature
      counter validation.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#5-feature-passkey-management-and-authentication-webauthn)
    * `TokenIssuingPort`: Responsible for creating and signing authentication tokens.
* **Repositories**:
    * `SessionRepository`: Manages the storage and retrieval of registration process related data.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)
    * `PasskeyRepository`: Manages the persistence of user passkey credentials.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#user-and-passkey-persistence)
    * `UserRepository`: Handles user data persistence and status updates.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#user-and-passkey-persistence)
