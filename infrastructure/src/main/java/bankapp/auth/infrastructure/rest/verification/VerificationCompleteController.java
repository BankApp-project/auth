package bankapp.auth.infrastructure.rest.verification;

import bankapp.auth.application.verification_complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.rest.verification.dto.CompleteVerificationRequest;
import bankapp.auth.infrastructure.rest.verification.dto.CompleteVerificationResponseDto;
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
        name = "app.feature.verification.complete.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/verification")
public class VerificationCompleteController {

    private final CompleteVerificationUseCase completeVerificationUseCase;

    @PostMapping("/complete/email/")
    public ResponseEntity<? extends CompleteVerificationResponseDto> completeEmailVerification(@RequestBody CompleteVerificationRequest request) {
        var email = new EmailAddress(request.email());
        var command = new CompleteVerificationCommand(email, request.value());

        var response = completeVerificationUseCase.handle(command);

        var responseDto = VerificationResponseMapper.toDto(response);

        return ResponseEntity.ok(responseDto);
    }

}


