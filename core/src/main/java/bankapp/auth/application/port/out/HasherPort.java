package bankapp.auth.application.port.out;

public interface HasherPort {
    String hashSecurely(String value);
}
