package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.exception.InvalidEmailFormatException;
import bankapp.auth.application.port.out.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitiateVerificationUseCaseTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    private EventPublisher eventPublisher;

    OtpGeneratorPort otpGenerator = new OtpGeneratorPort() {
        @Override
        public Otp generate(String email, int len) {
            return new Otp(generateNumericString(len), email);
        }

        private static String generateNumericString(int len) {
            return "1".repeat(len);
        }
    };

    @BeforeEach
    void setUp() {
        eventPublisher = mock(EventPublisher.class);
    }

    //    Test Case 1: Successful Verification Initiation
    //    Given: A user is on the login page
    //    When: A user provides email to `provide email` form and clicks `continue` button
    //    Then: User gets email msg containing otp

    //    Test Case 1: Successful Verification Initiation
    //    Given: A user is on the login page
    //    When: A user provides their valid email address and clicks 'continue'
    //    Then: An "verification-otp-generated" event should be published

    @Test
    void should_publish_event_when_provided_valid_email() {
        //given
        String validEmail = "test@bankapp.online";

        //when
        var command = new InitiateVerificationCommand(validEmail);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator);

        useCase.handle(command);
        //then
        verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }
/*
    Test Case 2: Invalid Email Format
    Given: A user is on the login page
    When: A user provides an invalidly formatted email address (e.g., "user@.com").
    Then: The command is rejected, and no event is published.
 */

    @ParameterizedTest
    @ValueSource(strings = {
            "test@.online",
            "test.dd.online",
            "testowy@email",
            "testmail",
            "test.mail",
    })
    void should_throw_exception_when_provided_invalid_email(String invalidEmail) {
        var command = new InitiateVerificationCommand(invalidEmail);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator);

        assertThrows(InvalidEmailFormatException.class, () -> useCase.handle(command));
    }

    @Test
    void should_use_correct_otp_length() {
        //given
        String validEmail = "test@bankapp.online";

        //when
        var command = new InitiateVerificationCommand(validEmail);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator);
        var result = useCase.handle(command);

        //then
        assertEquals(6, result.getValue().length(), "Use case should generate OTP with correct length");
    }

    @Test
    void should_generate_otp() {
        //given
        String validEmail = "test@bankapp.online";

        //when
        var command = new InitiateVerificationCommand(validEmail);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator);
        var result = useCase.handle(command);

        assertThat(result).isInstanceOf(Otp.class);
    }


}
// next tests:
//   OTP value should be hashed via HasherPort
//   OTP should be stored safely via otpRepositoryPort
//   OTP should be sent safely via commandBusPort
//    And: The event should contain the user's email
//    And: The event should contain a valid one-time password
//    And: The otp should contain N digit long code.

// OTP_LEN is outside range (4-8) should throw exception
