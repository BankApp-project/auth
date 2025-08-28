package bankapp.auth.persistance.otp.config;

import bankapp.auth.domain.port.out.OtpConfigPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OtpConfiguration implements OtpConfigPort {

    private final OtpProperties otpProperties;
    private final Clock clock;

    @Override
    public int getOtpSize() {
        return otpProperties.size();
    }

    @Override
    public Duration getTtl() {
        return otpProperties.ttl();
    }

    @Override
    public Clock getClock() {
        return clock;
    }
}
