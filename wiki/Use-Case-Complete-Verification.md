# Use Case: Complete Verification

This use case handles the final step of an email-based verification process, typically initiated after a user has
received and submitted a One-Time Password (OTP). Its primary function is to validate the OTP and then prepare for
either a new user registration or an existing user login using passkeys.

## Process Flow

The `handle` method orchestrates the verification completion process through the following steps:

1. **Verify and Consume OTP**: It first validates the provided OTP against the stored, hashed version.
    * It calls the `OtpRepository` to load the persisted `Otp` object by the user's email.
    * It then uses the `HashingPort` to securely compare the user-submitted OTP with the stored hash.
    * It uses the injectable `Clock` to ensure the `Otp` object has not expired.
    * Upon successful verification, it immediately calls `otpRepository.delete()` to consume the OTP, preventing any
      replay attacks.

2. **Find or Create User**: It queries the persistence layer via the `UserRepository` port to find a user by their email
   address.
    * If no user is found, it constructs a new `User` object (in a non-enabled state) and saves it via the
      `userRepository.save()` method.
    * If a user already exists, it proceeds with the existing user object.

3. **Generate and Persist Challenge**: To prepare for the WebAuthn ceremony, it manages a temporary challenge.
    * It calls the `ChallengeGenerationPort` to create a new, cryptographically secure `Challenge` object. This object
      contains a random value, a session ID, and an expiration time.
    * This `Challenge` is then persisted to the cache via the `ChallengeRepository` port, where it will be retrieved
      later in the flow.

4. **Prepare Passkey Response**: It inspects the `User` object's state to determine the correct passkey ceremony to
   initiate.
    * **For New Users** (who are not yet `enabled`): It calls the `credentialOptionsPort.getPasskeyCreationOptions()`
      method to generate the server-side options required to register a new passkey. This is returned in a
      `RegistrationResponse`.
    * **For Existing Users** (who are `enabled`): It first fetches their existing credentials via the
      `CredentialRepository`, then calls the `credentialOptionsPort.getPasskeyRequestOptions()` method with those
      credentials. This generates the options required for a passkey-based login, which is returned in a
      `LoginResponse`.

## Dependencies

This use case relies on a set of interfaces (Ports) to interact with external systems and data stores. The concrete
adapters that implement these ports are detailed in the main technical documentation.

* **Ports**:
    * `PasskeyOptionsPort`: An outgoing port responsible for generating the complex server-side options for WebAuthn
      registration or authentication ceremonies.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#passkey-options-generation)
    * `ChallengeGenerationPort`: An outgoing port used to create a cryptographically secure, time-bound challenge.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)
    * `HashingPort`: An outgoing port for securely verifying the user-submitted OTP against its stored hash.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#otp-hashing)
    * `SessionIdGenerationPort`: An outgoing port for generating unique session identifiers.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)

* **Repositories**:
    * `OtpRepository`: A port for saving, loading, and deleting `Otp` objects from persistence.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#otp-storage-and-repository)
    * `SessionRepository`: A port for saving and retrieving temporary `Session` objects from the cache.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)
    * `UserRepository`: A port for finding and creating `User` objects in the database.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#user-and-passkey-persistence)
    * `PasskeyRepository`: A port for loading a user's existing passkey credentials from the database.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#user-and-passkey-persistence)

* **Domain Services**:
    * `OtpService`: Encapsulates the business rules for OTP verification including timing and security validations.

* **Other System Beans**:
    * `Clock`: Provides a consistent source of time. Injecting a `Clock` is critical for deterministically testing
      time-sensitive logic like OTP expiration.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#serialization-and-system-beans)