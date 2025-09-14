package bankapp.auth.infrastructure.driving.rest.registration;

public record CompleteRegistrationRequest(
        String sessionId,
        String RegistrationResponseJSON
) {
}
