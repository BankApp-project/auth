package bankapp.auth.application.verification_complete.port.out;

public interface ChallengeGenerationPort {
    byte[] generate();
}