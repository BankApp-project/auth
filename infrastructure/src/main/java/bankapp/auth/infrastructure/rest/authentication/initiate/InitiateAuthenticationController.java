package bankapp.auth.infrastructure.rest.authentication.initiate;


import bankapp.auth.application.authentication.initiate.InitiateAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(
        name = "app.feature.authentication.initiate.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication/initiate")
public class InitiateAuthenticationController {

    private final InitiateAuthenticationUseCase initiateAuthenticationUseCase;

    @GetMapping
    public ResponseEntity<InitiateAuthenticationResponse> initiateAuthentication() {
        var useCaseResponse = initiateAuthenticationUseCase.handle();

        var response = new InitiateAuthenticationResponse(useCaseResponse.options(), useCaseResponse.challengeId().toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
