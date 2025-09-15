package bankapp.auth.application.shared.exception;

/// Exception thrown when malicious counter is detected in WebAuthn Verification Ceremony
public class MaliciousCounterException extends RuntimeException {
    public MaliciousCounterException(String msg) {
        super(msg);
    }
}
