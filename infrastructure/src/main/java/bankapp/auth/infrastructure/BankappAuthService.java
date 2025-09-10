package bankapp.auth.infrastructure;

import bankapp.auth.infrastructure.driven.challenge.config.ChallengeProperties;
import bankapp.auth.infrastructure.driven.otp.config.OtpProperties;
import bankapp.auth.infrastructure.driven.passkey.service.CredentialOptionsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({
        OtpProperties.class,
        ChallengeProperties.class,
        CredentialOptionsProperties.class
})
public class BankappAuthService {

    public static void main(String...args) {
        SpringApplication.run(BankappAuthService.class, args);
    }
}
