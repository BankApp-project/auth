package bankapp.auth.infrastructure.driving.rest.registration;

import bankapp.auth.application.registration.complete.CompleteRegistrationUseCase;
import bankapp.auth.application.registration.complete.port.in.CompleteRegistrationCommand;
import bankapp.auth.infrastructure.driving.rest.shared.dto.AuthenticationGrantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@ConditionalOnProperty(
        name = "app.feature.registration.complete.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/registration/complete")
@Tag(name = "User Registration", description = "Endpoints for the new user registration flow.")
public class CompleteRegistrationController {

    private final CompleteRegistrationUseCase completeRegistrationUseCase;

    @PostMapping
    @Operation(
            summary = "Complete New User Registration with Passkey",
            description = """
                    This is the final step in the new user registration flow, which is initiated **after a user has successfully verified their email via the `/verification/complete/*` endpoint** and received registration options.
                    
                    This endpoint receives the public key credential created by the user's authenticator (i.e., the response from the browser's `navigator.credentials.create()` call). The server verifies this response, permanently stores the new passkey credential, activates the user's account, and immediately logs them in by issuing authentication tokens.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registration successful. The user account has been created and activated, and authentication tokens are returned to establish a session.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationGrantResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request. The registration failed. This could be due to an invalid session, a malformed request, or if the passkey creation response from the authenticator could not be verified.",
                    content = @Content
            )
    })

    public ResponseEntity<AuthenticationGrantResponse> completeRegistation(@RequestBody CompleteRegistrationRequest request) {
        var challenge = UUID.fromString(request.sessionId());
        var regResponse = request.RegistrationResponseJSON();

        var command = new CompleteRegistrationCommand(challenge, regResponse);

        var useCaseResult = completeRegistrationUseCase.handle(command);
        var authTokens = useCaseResult.authTokens();

        var res = new AuthenticationGrantResponse(authTokens.accessToken(), authTokens.refreshToken());
        return ResponseEntity.ok(res);
    }
}

