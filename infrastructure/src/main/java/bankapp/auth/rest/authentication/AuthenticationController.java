package bankapp.auth.rest.authentication;

import bankapp.auth.application.authentication_complete.CompleteAuthenticationCommand;
import bankapp.auth.application.authentication_complete.CompleteAuthenticationUseCase;
import bankapp.auth.application.authentication_initiate.InitiateAuthenticationCommand;
import bankapp.auth.application.authentication_initiate.InitiateAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.rest.shared.dto.AuthenticationGrantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@ConditionalOnProperty(
        name = "app.feature.authentication.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    private final InitiateAuthenticationUseCase initiateAuthenticationUseCase;
    private final CompleteAuthenticationUseCase completeAuthenticationUseCase;

    @GetMapping("/initiate")
    public ResponseEntity<InitiateAuthenticationResponse> initiateAuthentication() {
        var command = new InitiateAuthenticationCommand();
        var useCaseResponse = initiateAuthenticationUseCase.handle(command);

        var response = new InitiateAuthenticationResponse(useCaseResponse.options(), useCaseResponse.challengeId().toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<AuthenticationGrantResponse> completeAuthentication(@RequestBody CompleteAuthenticationRequest request) {
        var command = getCompleteAuthenticationCommand(request);

        var useCaseResponse = completeAuthenticationUseCase.handle(command);

        var res = getAuthenticationGrantResponse(useCaseResponse);
        return ResponseEntity.ok(res);
    }

    private CompleteAuthenticationCommand getCompleteAuthenticationCommand(CompleteAuthenticationRequest request) {
        var challengeId = UUID.fromString(request.challengeId());
        var authRespJson = request.AuthenticationResponseJSON();
        var credentialId = request.credentialId();
        return new CompleteAuthenticationCommand(challengeId, authRespJson, credentialId);
    }

    private AuthenticationGrantResponse getAuthenticationGrantResponse(AuthenticationGrant useCaseResponse) {
        var authTokens = useCaseResponse.authTokens();
        return new AuthenticationGrantResponse(authTokens.accessToken(), authTokens.refreshToken());
    }
}
