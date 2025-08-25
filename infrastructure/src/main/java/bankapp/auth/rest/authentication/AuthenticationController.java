package bankapp.auth.rest.authentication;

import bankapp.auth.application.authentication_initiate.InitiateAuthenticationCommand;
import bankapp.auth.application.authentication_initiate.InitiateAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final InitiateAuthenticationUseCase initiateAuthenticationUseCase;

    @GetMapping("/initiate")
    public ResponseEntity<InitiateAuthenticationResponse> initiateAuthentication() {
        var command = new InitiateAuthenticationCommand();
        var useCaseResponse = initiateAuthenticationUseCase.handle(command);

        var response = new InitiateAuthenticationResponse(useCaseResponse.options(), useCaseResponse.challengeId().toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
