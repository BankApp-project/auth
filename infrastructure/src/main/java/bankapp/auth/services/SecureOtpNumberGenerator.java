package bankapp.auth.services;

import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class SecureOtpNumberGenerator implements OtpGenerationPort {

    private final SecureRandom secureRandom;

    @Override
    public String generate(int len) {
        verify(len);

        var randomOtpNumber = getRandomNumber(len);
        return String.valueOf(randomOtpNumber);
    }

    private void verify(int len) {
        if (len <= 0) {
            throw new OtpGenerationException("PIN length must be greater than 0");
        }

        if (len > 9) { // Prevent overflow
            throw new OtpGenerationException("PIN length cannot exceed 9 digits");
        }
    }

    private int getRandomNumber(int len) {
        // Generate a random number within the range, ensuring minimum length
        int lowerBound = (int) Math.pow(10, len - 1);
        int upperBound = (int) Math.pow(10, len);
        return secureRandom.nextInt(upperBound - lowerBound) + lowerBound;
    }

}
