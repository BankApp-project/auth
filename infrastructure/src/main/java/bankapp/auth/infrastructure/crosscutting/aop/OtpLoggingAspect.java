package bankapp.auth.infrastructure.crosscutting.aop;

import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@ConditionalOnBooleanProperty(name = "app.config.otp.console-enabled")
@Profile("!prod")
public class OtpLoggingAspect {

    @Before(value = "execution(* bankapp.auth.infrastructure.driven.notification.NotificationAdapter.sendOtpToUserEmail(..)) && args(userEmail, otpValue)", argNames = "userEmail,otpValue")
    public void logOtpToConsole(EmailAddress userEmail, String otpValue) {
        log.info("=================================================");
        log.info("OTP Code for {}: {}", userEmail.getValue(), otpValue);
        log.warn("=================================================");
        log.warn("This log is intended for development purposes only.");
        log.info("=================================================");
    }
}
