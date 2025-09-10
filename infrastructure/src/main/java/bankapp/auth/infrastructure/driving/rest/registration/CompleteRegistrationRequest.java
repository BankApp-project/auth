package bankapp.auth.infrastructure.driving.rest.registration;

public record CompleteRegistrationRequest(
        String challengeId,
        String RegistrationResponseJSON
) {
}
