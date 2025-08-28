package bankapp.auth.domain.port.out.stubs;

import bankapp.auth.domain.port.out.OtpConfigPort;

import java.time.Clock;

public class OtpConfig implements OtpConfigPort {

    private final int otpSize;
    private final int ttlInSeconds;
    private final Clock clock;

    public OtpConfig(Integer otpSize,
                     Integer ttlInSeconds, Clock clock) {
        this.otpSize = (otpSize == null || otpSize <= 0) ? 6 : otpSize;
        this.ttlInSeconds = (ttlInSeconds == null || ttlInSeconds <= 0) ? 150 : ttlInSeconds;
        this.clock = clock;
    }

    @Override
    public int getOtpSize() {
        return otpSize;
    }

    @Override
    public int getTtlInSeconds() {
        return ttlInSeconds;
    }

    @Override
    public Clock getClock() {
        return clock;
    }
}