package bankapp.auth.infrastructure.driving.rest.authentication.initiate;


import bankapp.auth.application.authentication.initiate.InitiateAuthenticationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(
        name = "app.feature.authentication.initiate.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication/initiate")
@Tag(name = "User Authentication", description = "Endpoints for managing user authentication flows.")
public class InitiateAuthenticationController {

    private final InitiateAuthenticationUseCase initiateAuthenticationUseCase;

    @GetMapping
    @Operation(
            summary = "Initiate Passkey (FIDO2/WebAuthn) Login",
            description = """
                    Initiates the authentication flow for a returning user on a trusted device, often referred to as the "happy path" for passkey login.
                    
                    This endpoint is triggered when the system recognizes the user (e.g., via a session cookie) and its purpose is to generate the FIDO2/WebAuthn challenge options required by the client (browser/authenticator) to request a passkey signature from the user.
                    
                    The client should use the `loginOptions` from the response to call the WebAuthn API (`navigator.credentials.get()`) and then send the result to the `/authentication/complete` endpoint.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated authentication options.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InitiateAuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. The user is not recognized or has no active session.",
                    content = @Content
            )
    })
    public ResponseEntity<InitiateAuthenticationResponse> initiateAuthentication() {
        try {
            MDC.put("operation", "initiate_authentication");

            log.info("Received authentication initiation request");

            var useCaseResponse = initiateAuthenticationUseCase.handle();

            log.info("Authentication initiation completed successfully");

            var response = new InitiateAuthenticationResponse(useCaseResponse.options(), useCaseResponse.sessionId().toString());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } finally {
            MDC.remove("operation");
        }
    }
}
