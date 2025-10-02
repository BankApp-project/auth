package bankapp.auth.application.shared.port.out.service;

public interface HashingPort {
    String hashSecurely(String value);

    boolean verify(String hashedValue, String value);
}
