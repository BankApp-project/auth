# Use Case: Initiate Authentication

This use case is responsible for initiating the "happy path" authentication flow for a returning user on a trusted
device. It is triggered when the user clicks "Continue" and the system has identified them as a known user (e.g., via a
cookie). Its sole purpose is to generate and provide the necessary FIDO2/WebAuthn options for the client to request a
passkey signature.

## Process Flow

The `handle` method orchestrates the authentication initiation process through the following steps:

1. **Generate and Persist Challenge**: It first calls the `ChallengeGenerationPort` to create a new, cryptographically
   secure `Challenge` object. This object contains a random value, a session ID, and an expiration time. This
   `Challenge` is then immediately persisted to the cache via the `ChallengeRepository` port. This is an essential
   security measure to prevent replay attacks during the subsequent `Complete Authentication` step.

2. **Generate Passkey Request Options**: It then calls the `credentialOptionsPort.getPasskeyRequestOptions()` method,
   passing in the newly created challenge. For this initial flow, no pre-existing user credentials are provided to the
   port. The port's implementation will then construct the `PublicKeyCredentialRequestOptions`, a standardized data
   structure that instructs the client's browser on how to perform the `navigator.credentials.get()` operation.

3. **Return Login Response**: The generated passkey request options and the unique session ID (from the challenge) are
   packaged into a `LoginResponse` object. This response is sent back to the client, which will use it to trigger the
   native browser or operating system authentication prompt.

## Dependencies

This use case relies on a set of interfaces (Ports) to perform its function. The concrete adapters that implement these
ports are detailed in the main technical documentation.

* **Ports**:
    * `ChallengeGenerationPort`: An outgoing port used to create a cryptographically secure, time-bound challenge.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)
    * `PasskeyOptionsPort`: An outgoing port responsible for generating the complex server-side options for WebAuthn
      authentication ceremonies.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#passkey-options-generation)
    * `SessionIdGenerationPort`: For generating unique session identifiers.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)

* **Repositories**:
    * `SessionRepository`: A port for saving and retrieving temporary `Session` objects from the cache.
        * [**View Technical Implementation Details
          **](https://github.com/BankApp-project/auth/wiki/Implementation-Details#challenge-generation-and-caching)