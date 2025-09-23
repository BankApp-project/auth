package bankapp.auth.infrastructure.driving.rest.authentication.initiate;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Response containing the necessary information to proceed with a WebAuthn passkey login ceremony.")
public record InitiateAuthenticationResponse(
        @Schema(description = "The FIDO2/WebAuthn server challenge and options (PublicKeyCredentialRequestOptions) that the client-side WebAuthn API needs to request a credential assertion from the authenticator.")
        PublicKeyCredentialRequestOptions loginOptions,

        @Schema(description = "A unique session identifier for this specific authentication attempt. This value must be sent back to the server during the finalization step.", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        String sessionId
) {
}
