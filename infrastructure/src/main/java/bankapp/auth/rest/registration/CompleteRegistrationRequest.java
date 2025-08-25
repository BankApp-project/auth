package bankapp.auth.rest.registration;

public record CompleteRegistrationRequest(
        String challengeId,
        String RegistrationResponseJSON
) {
}
