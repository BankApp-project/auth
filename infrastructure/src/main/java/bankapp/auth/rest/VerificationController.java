package bankapp.auth.rest;

import bankapp.auth.application.verification_initiate.InitiateVerificationUseCase;
import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController("/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final static Duration TIMEOUT_DURATION = Duration.ofSeconds(5);

    private final InitiateVerificationUseCase initiateVerificationUseCase;

    @PostMapping("/email/")
    public Mono<ResponseEntity<Void>> verifyEmail(@RequestBody InitiateVerificationCommand command) {

        // The background task is defined and subscribed to, kicking it off.
        Mono.fromRunnable(() -> initiateVerificationUseCase.handle(command))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(TIMEOUT_DURATION)
                .doOnError(e -> logVerifyEmailError(e, command.email()))
                .subscribe();

        // Immediately return 202 Accepted to the client.
        return Mono.just(ResponseEntity.accepted().build());
    }

    private void logVerifyEmailError(Throwable e, EmailAddress email) {
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
