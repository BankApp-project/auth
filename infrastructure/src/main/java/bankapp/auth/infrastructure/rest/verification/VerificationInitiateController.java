package bankapp.auth.infrastructure.rest.verification;


import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.rest.verification.dto.InitiateVerificationRequest;
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
public class VerificationInitiateController {

    private final AsyncInitiateVerificationService asyncInitiateVerificationService;

    @PostMapping("/email")
    public ResponseEntity<Void> initiateEmailVerification(@RequestBody InitiateVerificationRequest request) {
        var command = new InitiateVerificationCommand(new EmailAddress(request.email()));

        // Call the async method. This call returns immediately.
        asyncInitiateVerificationService.handle(command);

        // Immediately return 202 Accepted to the client.
        return ResponseEntity.accepted().build();
    }
}
