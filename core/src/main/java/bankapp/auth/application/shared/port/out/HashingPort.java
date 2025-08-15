package bankapp.auth.application.shared.port.out;

public interface HashingPort {
    String hashSecurely(String value);

    boolean verify(String hashedValue, String value);
}
