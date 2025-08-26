package bankapp.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BankappAuthService {

    public static void main(String...args) {
        SpringApplication.run(BankappAuthService.class, args);
    }
}
