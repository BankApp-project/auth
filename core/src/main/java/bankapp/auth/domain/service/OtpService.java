package bankapp.auth.domain.service;

import bankapp.auth.domain.model.Otp;

import java.security.SecureRandom;

public class OtpService {

    private final static SecureRandom random = new SecureRandom();

    public Otp generate(String validEmail, int len) {
        int otpValue = getOtpValue(len);

        return new Otp(String.valueOf(otpValue), validEmail);
    }

    private int getOtpValue(int len) {
        int min = (int) Math.pow(10, len -1);
        int max = (int) Math.pow(10, len);

        return min + random.nextInt(max - min);
    }
}
