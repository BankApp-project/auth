package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.HashingPort;
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.exception.InvalidEmailFormatException;
import bankapp.auth.application.port.out.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitiateVerificationUseCaseTest {

    private EventPublisher eventPublisher;
    private HashingPort hasher;
    private OtpGeneratorPort otpGenerator;

    private final static String VALID_EMAIL = "test@bankapp.online";
    private final static String DEFAULT_VALUE = "123456";
    private final static Otp DEFAULT_OTP = new Otp(DEFAULT_VALUE, VALID_EMAIL);
    private final static int DEFAULT_OTP_LEN = 6;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(EventPublisher.class);
        hasher = mock(HashingPort.class);
        otpGenerator = mock(OtpGeneratorPort.class);

        when(otpGenerator.generate(anyString(), anyInt())).thenReturn(DEFAULT_OTP);
        when(hasher.hashSecurely(anyString())).thenReturn(DEFAULT_VALUE + "-hashed");
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
        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher);

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
        //when
        var command = new InitiateVerificationCommand(invalidEmail);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher);

        //then
        assertThrows(InvalidEmailFormatException.class, () -> useCase.handle(command));
    }

    @Test
    void should_use_correct_otp_length() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher);
        var result = useCase.handle(command);

        //then
        assertEquals(DEFAULT_OTP_LEN, result.getValue().length(), "Use case should generate OTP with correct length");
    }

    @Test
    void should_generate_otp() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher);
        var result = useCase.handle(command);

        //then
        assertThat(result).isInstanceOf(Otp.class);
    }

    @Test
    void should_hash_otp() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher);

        useCase.handle(command);

        //then
        verify(hasher).hashSecurely(DEFAULT_VALUE);
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
