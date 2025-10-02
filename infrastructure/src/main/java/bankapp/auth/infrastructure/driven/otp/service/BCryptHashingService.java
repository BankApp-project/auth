package bankapp.auth.infrastructure.driven.otp.service;

import bankapp.auth.application.shared.port.out.service.HashingPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BCryptHashingService implements HashingPort {

    private final PasswordEncoder passwordEncoder;


    /**
     * Generates a secure hash of the provided input challenge using the configured password encoder.
     *
     * @param value the input string to be securely hashed; must not be null
     * @return the securely hashed representation of the input challenge
     */
    @Override
    public String hashSecurely(@NonNull String value) {
        log.info("Hashing value securely.");
        log.debug("Hashing value using BCrypt password encoder.");

        String hashedValue = passwordEncoder.encode(value);
        log.info("Successfully hashed value.");
        return hashedValue;
    }

    @Override
    public boolean verify(String hashedValue, String value) {
        log.info("Verifying hashed value.");
        log.debug("Verifying value against BCrypt hash.");

        boolean isValid = passwordEncoder.matches(value, hashedValue);
        log.info("Hash verification completed with result: {}", isValid);
        return isValid;
    }
}
