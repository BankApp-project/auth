package bankapp.auth.domain.port.out;

import java.time.Clock;
import java.time.Duration;

public interface OtpConfigPort {
    int getOtpSize();
    Duration getTtl();
    Clock getClock();
}
