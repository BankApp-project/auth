package bankapp.auth.infrastructure.driving.rest.authentication.complete;

import bankapp.auth.application.authentication.complete.CompleteAuthenticationCommand;
import bankapp.auth.application.authentication.complete.CompleteAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.infrastructure.driving.rest.shared.dto.AuthenticationGrantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@ConditionalOnProperty(
        name = "app.feature.authentication.complete.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication/complete")
public class CompleteAuthenticationController {

    private final CompleteAuthenticationUseCase completeAuthenticationUseCase;

    @PostMapping
    public ResponseEntity<AuthenticationGrantResponse> completeAuthentication(@RequestBody CompleteAuthenticationRequest request) {
        var command = getCompleteAuthenticationCommand(request);

        var useCaseResponse = completeAuthenticationUseCase.handle(command);

        var res = getAuthenticationGrantResponse(useCaseResponse);
        return ResponseEntity.ok(res);
    }

    private CompleteAuthenticationCommand getCompleteAuthenticationCommand(CompleteAuthenticationRequest request) {
        var sessionId = UUID.fromString(request.sessionId());
        var authRespJson = request.AuthenticationResponseJSON();
        var credentialId = request.credentialId();
        return new CompleteAuthenticationCommand(sessionId, authRespJson, credentialId);
    }

    private AuthenticationGrantResponse getAuthenticationGrantResponse(AuthenticationGrant useCaseResponse) {
        var authTokens = useCaseResponse.authTokens();
        return new AuthenticationGrantResponse(authTokens.accessToken(), authTokens.refreshToken());
    }
}
