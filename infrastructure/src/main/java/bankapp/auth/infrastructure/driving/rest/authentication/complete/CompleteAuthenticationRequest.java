package bankapp.auth.infrastructure.driving.rest.authentication.complete;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body to finalize a WebAuthn passkey authentication ceremony.")
public record CompleteAuthenticationRequest(
        @Schema(description = "The unique session identifier received from the `GET /authentication/initiate` endpoint.", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        String sessionId,

        @Schema(description = "The JSON-serialized response from the client-side WebAuthn API (`navigator.credentials.get()`). This object contains the signed challenge.", requiredMode = Schema.RequiredMode.REQUIRED)
        String AuthenticationResponseJSON,

        @Schema(description = "The unique identifier of the credential that was used for signing the challenge.", requiredMode = Schema.RequiredMode.REQUIRED, example = "01020304-0506-0708-090a-0b0c0d0e0f10")
        byte[] credentialId
) {
}
