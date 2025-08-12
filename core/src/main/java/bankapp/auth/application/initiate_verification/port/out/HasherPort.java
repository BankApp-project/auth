package bankapp.auth.application.initiate_verification.port.out;

public interface HasherPort {
    String hashSecurely(String value);
}
