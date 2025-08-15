package bankapp.auth.application.verify_otp.port.out;

public interface ChallengeGenerationPort {
    byte[] generate();
}