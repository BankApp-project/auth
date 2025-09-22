package bankapp.auth.infrastructure.driving.rest.verification.complete;

import bankapp.auth.application.verification.complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification.complete.port.in.CompleteVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driving.rest.verification.complete.dto.*;
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

@ConditionalOnProperty(
        name = "app.feature.verification.complete.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/verification")
@Tag(name = "User Verification", description = "Endpoints for managing user verification flows.")
public class VerificationCompleteController {

    private final CompleteVerificationUseCase completeVerificationUseCase;

    @PostMapping("/complete/email/")
    @Operation(
            summary = "Complete Email Verification with OTP",
            description = """
                    Handles the final step of the email verification process by validating the user-submitted One-Time Password (OTP).
                    
                    Based on whether the email address is recognized, this endpoint prepares for one of two outcomes:
                    1.  **Existing User**: The response provides the necessary options to initiate a **passkey login**.
                    2.  **New User**: The response provides the necessary options to create a new account via **passkey registration**.
                    
                    The `type` field in the response (`login` or `registration`) indicates which flow to proceed with.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP is valid. The response contains the next steps for either login or registration.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {LoginResponseDto.class, RegistrationResponseDto.class})
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. The provided OTP is invalid, expired, or does not match the email.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request. The request body is missing data or is malformed.",
                    content = @Content
            )
    })
    public ResponseEntity<? extends CompleteVerificationResponseDto> completeEmailVerification(@RequestBody CompleteVerificationRequest request) {
        var email = new EmailAddress(request.email());
        var command = new CompleteVerificationCommand(email, request.otpValue());

        var response = completeVerificationUseCase.handle(command);

        var responseDto = VerificationResponseMapper.toDto(response);

        return ResponseEntity.ok(responseDto);
    }

}
