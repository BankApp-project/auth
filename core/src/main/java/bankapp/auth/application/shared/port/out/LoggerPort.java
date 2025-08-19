package bankapp.auth.application.shared.port.out;

public interface LoggerPort {
    void info(String message);
    void info(String message, Object... arguments);
    void debug(String message);
    void debug(String message, Object... arguments);
    void warn(String message);
    void warn(String message, Object... arguments);
    void error(String message, Throwable throwable);
}
