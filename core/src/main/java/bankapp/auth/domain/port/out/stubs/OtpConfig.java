package bankapp.auth.domain.port.out.stubs;

import bankapp.auth.domain.port.out.OtpConfigPort;

public class OtpConfig implements OtpConfigPort {

    private final int otpSize;
    private final int ttlInSeconds;

    public OtpConfig(Integer otpSize, Integer ttlInSeconds) {
        this.otpSize = (otpSize == null || otpSize <= 0) ? 6 : otpSize;
        this.ttlInSeconds = (ttlInSeconds == null || ttlInSeconds <= 0) ? 150 : ttlInSeconds;
    }

    @Override
    public int getOtpSize() {
        return otpSize;
    }

    @Override
    public int getTtlInSeconds() {
        return ttlInSeconds;
    }
}