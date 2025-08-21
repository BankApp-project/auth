package bankapp.auth.application.verification_initiate;

import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import bankapp.auth.application.verification_initiate.port.out.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.shared.port.out.EventPublisherPort;
import bankapp.auth.application.shared.port.out.NotificationPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;
import bankapp.auth.domain.port.out.stubs.OtpConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitiateVerificationUseCaseTest {

    private final static EmailAddress VALID_EMAIL = new EmailAddress("test@bankapp.online");
    private final static String DEFAULT_VALUE = "123456";
    private final static String DEFAULT_HASHED_VALUE = DEFAULT_VALUE + "-hashed";
    private final static int DEFAULT_OTP_LEN = 6;
    private final static int DEFAULT_TTL = 10;
    private final static Clock DEFAULT_CLOCK = Clock.systemUTC();

    private EventPublisherPort eventPublisher;
    private HashingPort hasher;
    private OtpGenerationPort otpGenerator;
    private OtpRepository otpSaver;
    private NotificationPort notificationPort;

    private InitiateVerificationCommand command;
    private InitiateVerificationUseCase useCase;

    @BeforeEach
    void setUp() {
        otpGenerator = mock(OtpGenerationPort.class);
        hasher = mock(HashingPort.class);
        eventPublisher = mock(EventPublisherPort.class);
        otpSaver = mock(OtpRepository.class);
        notificationPort = mock(NotificationPort.class);
        OtpConfigPort otpConfig = new OtpConfig(DEFAULT_OTP_LEN, DEFAULT_TTL);
        OtpService otpService = new OtpService(otpGenerator, hasher, DEFAULT_CLOCK, otpConfig);

        command = new InitiateVerificationCommand(VALID_EMAIL);
        useCase = new InitiateVerificationUseCase(eventPublisher, otpSaver, notificationPort, otpService);

        when(otpGenerator.generate(anyInt())).thenReturn(DEFAULT_VALUE);
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
        useCase.handle(command);
        //then
        verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_hash_otp() {
        useCase.handle(command);

        //then
        verify(hasher).hashSecurely(DEFAULT_VALUE);
    }

    @Test
    void should_store_hashed_otp_in_repository() {
        useCase.handle(command);

        //then
        verify(otpSaver).save(argThat(otp ->
                otp.getValue().equals(DEFAULT_HASHED_VALUE) &&
                        otp.getKey().equals(VALID_EMAIL.toString())
        ));
    }

    @Test
    void should_send_otp_via_commandBusPort() {
        useCase.handle(command);

        verify(notificationPort).sendOtpToUserEmail(argThat(email ->
                email.equals(VALID_EMAIL.getValue())), argThat(otpValue ->
                otpValue.equals(DEFAULT_VALUE)));
    }

// BDD Test Cases: Should not send email to user when any of the steps fail

    @Test
    void should_not_send_email_when_otp_generation_fails() {
        // Given: OTP generation will fail
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("OTP generation failed"));

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(notificationPort, never()).sendOtpToUserEmail(anyString(), anyString());
    }

    @Test
    void should_not_send_email_when_hashing_fails() {
        // Given: Hashing will fail
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(notificationPort, never()).sendOtpToUserEmail(anyString(), anyString());
    }

    @Test
    void should_not_send_email_when_otp_saving_fails() {
        // Given: OTP saving will fail
        doThrow(new RuntimeException("Database save failed")).when(otpSaver).save(any(Otp.class));

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(notificationPort, never()).sendOtpToUserEmail(anyString(), anyString());
    }


    // Scenario: Multiple step failures
    @Test
    void should_not_send_email_when_multiple_steps_fail() {
        // Given: Multiple operations will fail
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("OTP generation failed"));
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(notificationPort, never()).sendOtpToUserEmail(anyString(), anyString());
    }

    @Test
    void should_only_publish_event_after_otp_saving_completes_successfully() {
        // When: The use case is executed
        useCase.handle(command);

        // Then: Event should be published only after OTP saving
        InOrder inOrder = inOrder(otpSaver, eventPublisher);
        inOrder.verify(otpSaver).save(any(Otp.class));
        inOrder.verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

// BDD Test Cases: Should not publish event when any of the previous steps fail

    @Test
    void should_not_publish_event_when_otp_generation_fails() {
        // Given: OTP generation will fail
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("OTP generation failed"));

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: No event should be published
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_not_publish_event_when_hashing_fails() {
        // Given: Hashing will fail
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: No event should be published
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_not_publish_event_when_otp_saving_fails() {
        // Given: OTP saving will fail
        doThrow(new RuntimeException("Database save failed")).when(otpSaver).save(any(Otp.class));

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: No event should be published
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_not_publish_event_when_any_prerequisite_step_fails() {
        // Scenario: Testing with OTP generation failure as representative case
        // Given: A prerequisite step will fail
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("Step failed"));

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: Event publishing should never be attempted
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));

        // And: Subsequent steps should not be executed
        verify(hasher, never()).hashSecurely(anyString());
        verify(otpSaver, never()).save(any(Otp.class));
    }

    // Scenario: Verify event contains correct data when all steps succeed
    @Test
    void should_publish_event_with_correct_data_when_all_steps_succeed() {
        // When: The use case is executed successfully
        useCase.handle(command);

        // Then: Event should be published with correct email and OTP data
        verify(eventPublisher).publish(argThat(event -> {
            if (event instanceof EmailVerificationOtpGeneratedEvent otpEvent) {
                return VALID_EMAIL.toString().equals(otpEvent.getEmail()) &&
                        DEFAULT_HASHED_VALUE.equals(otpEvent.getOtpValue());
            }
            return false;
        }));
    }

    @Test
    void should_pass_ttl_with_persistance_request() {
        // When: The use case is executed successfully
        useCase.handle(command);

        verify(otpSaver).save(any(Otp.class));
    }

// BDD Test Cases: Should throw InitiateVerificationException when any of the steps fail

    @Test
    void should_throw_InitiateVerificationException_when_otp_generation_fails() {
        // Given: OTP generation will fail
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("OTP generation failed"));

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_hashing_fails() {
        // Given: Hashing will fail
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_otp_saving_fails() {
        // Given: OTP saving will fail
        doThrow(new RuntimeException("Database save failed")).when(otpSaver).save(any(Otp.class));

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_event_publishing_fails() {
        // Given: Event publishing will fail
        doThrow(new RuntimeException("Event publishing failed")).when(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_command_bus_fails() {
        // Given: Command bus will fail
        doThrow(new RuntimeException("Command bus failed")).when(notificationPort).sendOtpToUserEmail(anyString(), anyString());

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    // Scenario: Multiple step failures should still throw InitiateVerificationException
    @Test
    void should_throw_InitiateVerificationException_when_multiple_steps_fail() {
        // Given: Multiple operations will fail
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("OTP generation failed"));
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    // BDD Test Case: Should include meaningful error message in exception
    @Test
    void should_throw_InitiateVerificationException_with_meaningful_message_when_otp_generation_fails() {
        // Given: OTP generation will fail with specific error
        when(otpGenerator.generate(anyInt())).thenThrow(new RuntimeException("Database connection timeout"));

        // When & Then: Should throw InitiateVerificationException with meaningful message
        InitiateVerificationException exception = assertThrows(InitiateVerificationException.class,
                () -> useCase.handle(command));

        assertThat(exception.getMessage()).contains("Failed to initiate verification");
    }

    @Test
    void should_set_ttl_to_persisted_otp() {
        // When: The use case is executed successfully
        useCase.handle(command);

        verify(otpSaver).save(
                argThat(otp -> otp.getExpirationTime() != null));
    }

    @Test
    void should_set_ttl_to_persisted_otp_correctly() {
        // When: The use case is executed successfully
        useCase.handle(command);

        Instant justBefore = Instant.now().minusSeconds((DEFAULT_TTL * 60 - 1));
        Instant justAfter = Instant.now().plusSeconds((DEFAULT_TTL * 60 + 1));

        verify(otpSaver).save(argThat(otp ->
                otp.getExpirationTime().isAfter(justBefore) &&
                        otp.getExpirationTime().isBefore(justAfter)
        ));
    }
}
// next tests:
// - ???
/*
notes: email will be sent to user even if publishing event `EmailVerificationOtpGeneratedEvent` fails.
 */