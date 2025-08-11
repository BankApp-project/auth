package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.HashingPort;
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.application.port.out.OtpRepositoryPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.application.port.out.EventPublisher;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitiateVerificationUseCaseTest {

    private EventPublisher eventPublisher;
    private HashingPort hasher;
    private OtpGeneratorPort otpGenerator;
    private OtpRepositoryPort otpRepository;

    private final static EmailAddress VALID_EMAIL = new EmailAddress("test@bankapp.online");
    private final static String DEFAULT_VALUE = "123456";
    private final static String DEFAULT_HASHED_VALUE = DEFAULT_VALUE + "-hashed";
    private final static Otp DEFAULT_OTP = new Otp(DEFAULT_VALUE, VALID_EMAIL.toString());
    private final static int DEFAULT_OTP_LEN = 6;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(EventPublisher.class);
        hasher = mock(HashingPort.class);
        otpGenerator = mock(OtpGeneratorPort.class);
        otpRepository = mock(OtpRepositoryPort.class);

        when(otpGenerator.generate(anyString(), anyInt())).thenReturn(DEFAULT_OTP);
        when(hasher.hashSecurely(anyString())).thenReturn(DEFAULT_HASHED_VALUE);
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
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpRepository, DEFAULT_OTP_LEN);

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


    @Test
    void should_use_correct_otp_length() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpRepository, DEFAULT_OTP_LEN);
        var result = useCase.handle(command);

        //then
        assertEquals(DEFAULT_OTP_LEN, result.getValue().length(), "Use case should generate OTP with correct length");
    }

    @Test
    void should_generate_otp() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpRepository, DEFAULT_OTP_LEN);
        var result = useCase.handle(command);

        //then
        assertThat(result).isInstanceOf(Otp.class);
    }

    @Test
    void should_hash_otp() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpRepository, DEFAULT_OTP_LEN);

        useCase.handle(command);

        //then
        verify(hasher).hashSecurely(DEFAULT_VALUE);
    }

    @Test
    void should_store_hashed_otp_in_repository() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpRepository, DEFAULT_OTP_LEN);

        useCase.handle(command);

        //then
        verify(otpRepository).save(argThat(otp ->
                otp.getValue().equals(DEFAULT_HASHED_VALUE) &&
                otp.getKey().equals(VALID_EMAIL.toString())
        ));
    }
}
// next tests:
//   OTP should be sent via commandBusPort
//    And: The otp should contain N digit long code.

// OTP_LEN is outside range (4-8) should throw exception