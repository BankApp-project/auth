package bankapp.auth.infrastructure.rest.verification;

import bankapp.auth.application.verification_complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.rest.verification.dto.CompleteVerificationRequest;
import bankapp.auth.infrastructure.rest.verification.dto.CompleteVerificationResponseDto;
import bankapp.auth.infrastructure.rest.verification.dto.InitiateVerificationRequest;
import bankapp.auth.infrastructure.rest.verification.dto.VerificationResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(
        name = "app.feature.verification.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/verification")
public class VerificationController {

    private final AsyncInitiateVerificationService asyncInitiateVerificationService;
    private final CompleteVerificationUseCase completeVerificationUseCase;

    @PostMapping("/initiate/email/")
    public ResponseEntity<Void> initiateEmailVerification(@RequestBody InitiateVerificationRequest request) {
        var command = new InitiateVerificationCommand(new EmailAddress(request.email()));

        // Call the async method. This call returns immediately.
        asyncInitiateVerificationService.handle(command);

        // Immediately return 202 Accepted to the client.
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/complete/email/")
    public ResponseEntity<? extends CompleteVerificationResponseDto> completeEmailVerification(@RequestBody CompleteVerificationRequest request) {
        var email = new EmailAddress(request.email());
        var command = new CompleteVerificationCommand(email, request.value());

        var response = completeVerificationUseCase.handle(command);

        var responseDto = VerificationResponseMapper.toDto(response);

        return ResponseEntity.ok(responseDto);
    }

}


