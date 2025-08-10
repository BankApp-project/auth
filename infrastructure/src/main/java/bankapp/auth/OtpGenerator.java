package bankapp.auth;

//is it right? to have dependency on core domain in infra?
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.domain.model.Otp;

import java.security.SecureRandom;

public class OtpGenerator implements OtpGeneratorPort {

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
