package bankapp.auth.rest.registration;

import bankapp.auth.application.registration_complete.CompleteRegistrationUseCase;
import bankapp.auth.application.registration_complete.port.in.CompleteRegistrationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final CompleteRegistrationUseCase completeRegistrationUseCase;

    @PostMapping("/complete")
    public ResponseEntity<AuthenticationGrantResponse> completeRegistation(@RequestBody CompleteRegistrationRequest request) {
        var challenge = UUID.fromString(request.challengeId());
        var regResponse = request.RegistrationResponseJSON();

        var command = new CompleteRegistrationCommand(challenge, regResponse);

        var useCaseResult = completeRegistrationUseCase.handle(command);
        var authTokens = useCaseResult.authTokens();

        var res = new AuthenticationGrantResponse(authTokens.accessToken(), authTokens.refreshToken());
        return ResponseEntity.ok(res);
    }
}
