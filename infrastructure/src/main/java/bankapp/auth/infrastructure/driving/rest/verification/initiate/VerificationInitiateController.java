package bankapp.auth.infrastructure.driving.rest.verification.initiate;


import bankapp.auth.application.verification.initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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

@ConditionalOnProperty(
        name = "app.feature.verification.initiate.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/verification/initiate")
@Tag(name = "User Verification", description = "Endpoints for managing user verification flows.")
public class VerificationInitiateController {

    private final AsyncInitiateVerificationService asyncInitiateVerificationService;

    @PostMapping("/email")
    @Operation(
            summary = "Initiate Email Verification",
            description = """
                    Starts the email-based verification flow by generating and sending a One-Time Password (OTP) to the provided email address.
                    
                    This endpoint is asynchronous. Upon receiving a valid request, it immediately returns an `HTTP 202 Accepted` response to acknowledge that the request has been received and is being processed in the background. It does not wait for the email to be sent.
                    
                    This is typically used during new user registration or when a user logs in from an unrecognized device.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Accepted for processing. The verification email will be sent asynchronously.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request. The request body is invalid (e.g., malformed email address).",
                    content = @Content
            )
    })
    public ResponseEntity<Void> initiateEmailVerification(@RequestBody InitiateVerificationRequest request) {
        var command = new InitiateVerificationCommand(new EmailAddress(request.email()));

        // Call the async method. This call returns immediately.
        asyncInitiateVerificationService.handle(command);

        // Immediately return 202 Accepted to the client.
        return ResponseEntity.accepted().build();
    }
}
