package bankapp.auth.domain.port.out.stubs;

import bankapp.auth.domain.port.out.OtpConfigPort;

public class OtpConfig implements OtpConfigPort {

    private final int otpSize;
    private final int ttlInMinutes;

    public OtpConfig(Integer otpSize, Integer ttlInMinutes) {
        this.otpSize = (otpSize == null || otpSize <= 0) ? 6 : otpSize;
        this.ttlInMinutes = (ttlInMinutes == null || ttlInMinutes <= 0) ? 10 : ttlInMinutes;
    }

    @Override
    public int getOtpSize() {
        return otpSize;
    }

    @Override
    public int getTtlInMinutes() {
        return ttlInMinutes;
    }
}