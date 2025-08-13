package bankapp.auth.application.initiate_verification.port.out;

public interface HashingPort {
    String hashSecurely(String value);

    boolean verify(String hashedValue, String value);
}
