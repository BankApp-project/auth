# `InitiateAuthenticationUseCase`

This use case is responsible for initiating the "happy path" authentication flow for a returning user on a trusted device. It is triggered when the user clicks "Continue" and the system has identified them as a known user (e.g., via a cookie). Its sole purpose is to generate and provide the necessary FIDO2/WebAuthn options for the client to request a passkey signature.

## Process Flow

The `handle` method orchestrates the authentication initiation process through the following steps:

1.  **Generate and Store Challenge**: A new, cryptographically secure, and time-limited challenge is generated. This challenge is immediately saved to the `ChallengeRepository` to be validated in the subsequent completion step, which is essential for preventing replay attacks.

2.  **Generate Passkey Request Options**: It calls the `CredentialOptionsPort` to create the `PublicKeyCredentialRequestOptions`. This is a standardized data structure that instructs the client's browser on how to perform the `navigator.credentials.get()` operation, prompting the user for their passkey (e.g., via biometrics or PIN).

3.  **Return Login Response**: The generated passkey request options and the unique session ID (from the challenge) are packaged into a `LoginResponse` object. This response is sent back to the client, which will use it to trigger the native browser/OS authentication prompt.

## Dependencies

This use case relies on a set of interfaces (Ports) and repositories to perform its function:

*   **Ports**:
    *   `ChallengeGenerationPort`: For creating secure, random challenges.
    *   `CredentialOptionsPort`: To generate the FIDO2/WebAuthn-compliant passkey request options.
*   **Repositories**:
    *   `ChallengeRepository`: Manages the storage and retrieval of authentication challenges.
*   **Other**:
    *   `Clock`: Provides the current time for setting the challenge's expiration.
    *   `challengeTtl`: A configuration parameter specifying the duration for which the challenge is valid.