package bankapp.auth.domain.port.out;

public interface OtpConfigPort {
    int getOtpSize();
    int getTtlInMinutes();
}
