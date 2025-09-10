package bankapp.auth.infrastructure;

import bankapp.auth.infrastructure.driven.challenge.config.ChallengeProperties;
import bankapp.auth.infrastructure.driven.otp.config.OtpProperties;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyRpProperties;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeySecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({
        OtpProperties.class,
        ChallengeProperties.class,
        PasskeyRpProperties.class,
        PasskeySecurityProperties.class
})
public class BankappAuthService {

    static void main(String... args) {
        SpringApplication.run(BankappAuthService.class, args);
    }
}
