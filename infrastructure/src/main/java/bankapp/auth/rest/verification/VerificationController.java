package bankapp.auth.rest.verification;

import bankapp.auth.application.verification_complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_initiate.InitiateVerificationUseCase;
import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.rest.verification.dto.CompleteVerificationRequest;
import bankapp.auth.rest.verification.dto.CompleteVerificationResponseDto;
import bankapp.auth.rest.verification.dto.InitiateVerificationRequest;
import bankapp.auth.rest.verification.dto.VerificationResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final static Duration TIMEOUT_DURATION = Duration.ofSeconds(5);

    private final InitiateVerificationUseCase initiateVerificationUseCase;
    private final CompleteVerificationUseCase completeVerificationUseCase;

    @PostMapping("/initiate/email/")
    public Mono<ResponseEntity<Void>> initiateEmailVerification(@RequestBody InitiateVerificationRequest request) {

        var command = new InitiateVerificationCommand(new EmailAddress(request.email()));

        // The background task is defined and subscribed to, kicking it off.
        Mono.fromRunnable(() -> initiateVerificationUseCase.handle(command))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(TIMEOUT_DURATION)
                .doOnError(e -> logEmailVerificationError(e, command.email()))
                .subscribe();

        // Immediately return 202 Accepted to the client.
        return Mono.just(ResponseEntity.accepted().build());
    }

    @PostMapping("/complete/email/")
    public Mono<ResponseEntity<? extends CompleteVerificationResponseDto>> completeEmailVerification(@RequestBody CompleteVerificationRequest request) {
        var email = new EmailAddress(request.email());
        var command = new CompleteVerificationCommand(email, request.value());

        return Mono.fromCallable(() -> completeVerificationUseCase.handle(command))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(TIMEOUT_DURATION)
                .doOnError(e -> logEmailVerificationError(e, command.key()))
                .map(VerificationResponseMapper::toDto)
                .map(ResponseEntity::ok);
    }

    private void logEmailVerificationError(Throwable e, EmailAddress email) {
        if (e instanceof TimeoutException) {
            logTimeoutException(e, email);
        } else {
            logApplicationException(e, email);
        }
    }

    private void logTimeoutException(Throwable e, EmailAddress email) {
        log.error("TIMEOUT: Verification for email {} took too long to process.", email.getValue(), e);
    }

    private void logApplicationException(Throwable e, EmailAddress email) {
        log.error("Failed to process email verification for email: {}", email.getValue(), e);
    }
}
