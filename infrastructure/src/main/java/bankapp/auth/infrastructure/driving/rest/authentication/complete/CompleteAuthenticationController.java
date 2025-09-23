package bankapp.auth.infrastructure.driving.rest.authentication.complete;

import bankapp.auth.application.authentication.complete.CompleteAuthenticationCommand;
import bankapp.auth.application.authentication.complete.CompleteAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.infrastructure.driving.rest.shared.dto.AuthenticationGrantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@ConditionalOnProperty(
        name = "app.feature.authentication.complete.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication/complete")
@Tag(name = "User Authentication", description = "Endpoints for managing user authentication flows.")
public class CompleteAuthenticationController {

    private final CompleteAuthenticationUseCase completeAuthenticationUseCase;

    @PostMapping
    @Operation(
            summary = "Complete Passkey (FIDO2/WebAuthn) Login",
            description = """
                    Finalizes the passkey authentication process by validating the user's signed challenge. This is the last step in any successful login flow.
                    
                    This endpoint is called after a login has been initiated in one of two ways:
                    1.  **Directly**, via the `GET /authentication/initiate` endpoint for a recognized returning user (the "happy path").
                    2.  **After email verification**, via the `POST /verification/complete/*` endpoint, when the system identifies an existing user and returns a `login` type response.
                    
                    The server validates the signed credential from the client. If valid, the user is authenticated, and the server returns a set of authentication tokens (`accessToken` and `refreshToken`).
                    """
    )

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful. Access and refresh tokens are returned.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationGrantResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. The authentication failed due to an invalid signature, expired session, or mismatched challenge.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request. The request body is missing required fields or is improperly formatted.",
                    content = @Content
            )
    })
    public ResponseEntity<AuthenticationGrantResponse> completeAuthentication(@RequestBody CompleteAuthenticationRequest request) {
        var command = getCompleteAuthenticationCommand(request);

        var useCaseResponse = completeAuthenticationUseCase.handle(command);

        var res = getAuthenticationGrantResponse(useCaseResponse);
        return ResponseEntity.ok(res);
    }

    private CompleteAuthenticationCommand getCompleteAuthenticationCommand(CompleteAuthenticationRequest request) {
        var sessionId = UUID.fromString(request.sessionId());
        var authRespJson = request.AuthenticationResponseJSON();
        var credentialId = request.credentialId();
        return new CompleteAuthenticationCommand(sessionId, authRespJson, credentialId);
    }

    private AuthenticationGrantResponse getAuthenticationGrantResponse(AuthenticationGrant useCaseResponse) {
        var authTokens = useCaseResponse.authTokens();
        return new AuthenticationGrantResponse(authTokens.accessToken(), authTokens.refreshToken());
    }
}
