package bankapp.auth.infrastructure;

import bankapp.auth.infrastructure.persistance.otp.config.OtpProperties;
import bankapp.auth.infrastructure.services.challenge.ChallengeProperties;
import bankapp.auth.infrastructure.services.credential_options.CredentialOptionsProperties;
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
