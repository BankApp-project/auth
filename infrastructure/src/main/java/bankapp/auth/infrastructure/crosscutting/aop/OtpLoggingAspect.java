package bankapp.auth.infrastructure.crosscutting.aop;

import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OtpLoggingAspect {

    @Before(value = "execution(* bankapp.auth.infrastructure.driven.notification.NotificationAdapter.sendOtpToUserEmail(..)) && args(userEmail, otpValue)", argNames = "userEmail,otpValue")
    public void logOtpToConsole(EmailAddress userEmail, String otpValue) {
        log.info("=================================================");
        log.info("OTP Code for {}: {}", userEmail.getValue(), otpValue);
        log.info("=================================================");
    }
}
