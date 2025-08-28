package bankapp.auth.config;

import bankapp.auth.domain.port.out.OtpConfigPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
public class OtpProperties implements OtpConfigPort {

    private final int otpSize;
    private final Duration ttl;
    private final Clock clock;

    public OtpProperties(
            @Value("${app.config.otp.size}") int otpSize,
            @Value("${app.config.otp.ttlInSeconds}") long ttlInSeconds,
            @Autowired Clock clock) {

        this.otpSize = otpSize;
        this.ttl = Duration.ofSeconds(ttlInSeconds);
        this.clock = clock;
    }

    @Override
    public int getOtpSize() {
        return otpSize;
    }

    @Override
    public Duration getTtl() {
        return ttl;
    }

    @Override
    public Clock getClock() {
        return clock;
    }
}
