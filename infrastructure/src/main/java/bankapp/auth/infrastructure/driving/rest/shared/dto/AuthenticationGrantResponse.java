package bankapp.auth.infrastructure.driving.rest.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing authentication tokens granted upon successful login.")
public record AuthenticationGrantResponse(
        @Schema(description = "A short-lived JSON Web Token (JWT) used to authenticate subsequent API requests.")
        String accessToken,

        @Schema(description = "A long-lived token used to obtain a new access token without requiring the user to log in again.")
        String refreshToken
) {
}
