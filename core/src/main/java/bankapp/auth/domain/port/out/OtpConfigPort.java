package bankapp.auth.domain.port.out;

import java.time.Clock;

public interface OtpConfigPort {
    int getOtpSize();
    long getTtlInSeconds();
    Clock getClock();
}
