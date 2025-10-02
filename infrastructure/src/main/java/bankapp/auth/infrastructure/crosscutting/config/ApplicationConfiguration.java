package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.service.HashingPort;
import bankapp.auth.application.verification.initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.port.out.OtpConfigPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@ComponentScan(
        basePackages = {
                "bankapp.auth.infrastructure",
                "bankapp.auth.application"
        },
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                value = {
                        UseCase.class
                }
        )
)
@Configuration
public class ApplicationConfiguration {

    @Bean
    public OtpService otpService(
            OtpGenerationPort otpGenerator,
            HashingPort hasher,
            OtpConfigPort config,
            OtpRepository repository) {
        return new OtpService(otpGenerator, hasher, config, repository);
    }
}
