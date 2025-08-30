package bankapp.auth.infrastructure.rest.registration;

public record CompleteRegistrationRequest(
        String challengeId,
        String RegistrationResponseJSON
) {
}
