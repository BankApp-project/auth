package bankapp.auth;

import bankapp.auth.config.OtpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(OtpProperties.class)
public class BankappAuthService {

    public static void main(String...args) {
        SpringApplication.run(BankappAuthService.class, args);
    }
}
