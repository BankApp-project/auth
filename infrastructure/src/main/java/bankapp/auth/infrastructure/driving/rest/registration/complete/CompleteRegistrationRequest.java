package bankapp.auth.infrastructure.driving.rest.registration.complete;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body to finalize the new user registration by submitting the newly created passkey credential.")
public record CompleteRegistrationRequest(
        @Schema(description = "The unique session identifier received from the `POST /registration/initiate` endpoint. This is used to verify the registration attempt.", requiredMode = Schema.RequiredMode.REQUIRED, example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        String sessionId,

        @Schema(description = "The JSON-serialized response from the client-side WebAuthn API (`navigator.credentials.create()`). This object contains the new public key credential and attestation data.", requiredMode = Schema.RequiredMode.REQUIRED)
        String RegistrationResponseJSON
) {
}
