package bankapp.auth.infrastructure.services.otp;

import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class SecureOtpNumberGenerator implements OtpGenerationPort {

    private final SecureRandom secureRandom;

    /**
     * Generates a random numeric OTP (One-Time Password) string of the specified length.
     * The method ensures that the generated OTP has the exact requested length, and throws
     * an exception if the length is invalid (less than or equal to zero or greater than 9).
     *
     * @param len the desired length of the OTP; must be a positive integer between 1 and 9 (inclusive)
     * @return a string representing the generated OTP of the specified length
     * @throws OtpGenerationException if the specified length is invalid
     */
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
