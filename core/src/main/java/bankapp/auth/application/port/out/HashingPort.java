package bankapp.auth.application.port.out;

public interface HashingPort {
    String hashSecurely(String value);
}
