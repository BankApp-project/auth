package bankapp.auth.application.usecase;

import bankapp.auth.application.port.in.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.*;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitiateVerificationUseCaseTest {

    private EventPublisherPort eventPublisher;
    private HasherPort hasher;
    private OtpGeneratorPort otpGenerator;
    private OtpSaverPort otpSaver;
    private CommandBus commandBus;

    private final static EmailAddress VALID_EMAIL = new EmailAddress("test@bankapp.online");
    private final static String DEFAULT_VALUE = "123456";
    private final static String DEFAULT_HASHED_VALUE = DEFAULT_VALUE + "-hashed";
    private final static Otp DEFAULT_OTP = new Otp(DEFAULT_VALUE, VALID_EMAIL.toString());
    private final static int DEFAULT_OTP_LEN = 6;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(EventPublisherPort.class);
        hasher = mock(HasherPort.class);
        otpGenerator = mock(OtpGeneratorPort.class);
        otpSaver = mock(OtpSaverPort.class);
        commandBus = mock(CommandBus.class);

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
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

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
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);
        var result = useCase.handle(command);

        //then
        assertEquals(DEFAULT_OTP_LEN, result.getValue().length(), "Use case should generate OTP with correct length");
    }

    @Test
    void should_generate_otp() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);
        var result = useCase.handle(command);

        //then
        assertThat(result).isInstanceOf(Otp.class);
    }

    @Test
    void should_hash_otp() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        useCase.handle(command);

        //then
        verify(hasher).hashSecurely(DEFAULT_VALUE);
    }

    @Test
    void should_store_hashed_otp_in_repository() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        useCase.handle(command);

        //then
        verify(otpSaver).save(argThat(otp ->
                otp.getValue().equals(DEFAULT_HASHED_VALUE) &&
                otp.getKey().equals(VALID_EMAIL.toString())
        ));
    }

    @Test
    void should_send_otp_via_commandBusPort() {

        //when
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        useCase.handle(command);

        verify(commandBus).sendOtpToUserEmail(argThat(email ->
                email.equals(VALID_EMAIL.getValue())), argThat(otpValue ->
                otpValue.equals(DEFAULT_VALUE)));
    }

// BDD Test Cases: Should not send email to user when any of the steps fail

    @Test
    void should_not_send_email_when_otp_generation_fails() {
        // Given: OTP generation will fail
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("OTP generation failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(commandBus, never()).sendOtpToUserEmail(anyString(), anyString());
    }

    @Test
    void should_not_send_email_when_hashing_fails() {
        // Given: Hashing will fail
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(commandBus, never()).sendOtpToUserEmail(anyString(), anyString());
    }

    @Test
    void should_not_send_email_when_otp_saving_fails() {
        // Given: OTP saving will fail
        doThrow(new RuntimeException("Database save failed")).when(otpSaver).save(any(Otp.class));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(commandBus, never()).sendOtpToUserEmail(anyString(), anyString());
    }


    // Scenario: Multiple step failures
    @Test
    void should_not_send_email_when_multiple_steps_fail() {
        // Given: Multiple operations will fail
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("OTP generation failed"));
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(RuntimeException.class, () -> useCase.handle(command));

        // Then: No email should be sent to the user
        verify(commandBus, never()).sendOtpToUserEmail(anyString(), anyString());
    }

// BDD Test Cases: Should only publish event when all previous steps completed successfully

    @Test
    void should_only_publish_event_after_otp_generation_completes_successfully() {
        // Given: All operations will succeed
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed
        useCase.handle(command);

        // Then: Event should be published only after OTP generation
        InOrder inOrder = inOrder(otpGenerator, eventPublisher);
        inOrder.verify(otpGenerator).generate(VALID_EMAIL.toString(), DEFAULT_OTP_LEN);
        inOrder.verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_only_publish_event_after_otp_hashing_completes_successfully() {
        // Given: All operations will succeed
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed
        useCase.handle(command);

        // Then: Event should be published only after OTP hashing
        InOrder inOrder = inOrder(hasher, eventPublisher);
        inOrder.verify(hasher).hashSecurely(DEFAULT_VALUE);
        inOrder.verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_only_publish_event_after_otp_saving_completes_successfully() {
        // Given: All operations will succeed
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed
        useCase.handle(command);

        // Then: Event should be published only after OTP saving
        InOrder inOrder = inOrder(otpSaver, eventPublisher);
        inOrder.verify(otpSaver).save(any(Otp.class));
        inOrder.verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_follow_correct_execution_order_before_publishing_event() {
        // Given: All operations will succeed
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed
        useCase.handle(command);

        // Then: All prerequisite steps should complete before event publishing
        InOrder inOrder = inOrder(otpGenerator, hasher, otpSaver, eventPublisher);
        inOrder.verify(otpGenerator).generate(VALID_EMAIL.toString(), DEFAULT_OTP_LEN);
        inOrder.verify(hasher).hashSecurely(DEFAULT_VALUE);
        inOrder.verify(otpSaver).save(any(Otp.class));
        inOrder.verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

// BDD Test Cases: Should not publish event when any of the previous steps fail

    @Test
    void should_not_publish_event_when_otp_generation_fails() {
        // Given: OTP generation will fail
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("OTP generation failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: No event should be published
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_not_publish_event_when_hashing_fails() {
        // Given: Hashing will fail
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: No event should be published
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_not_publish_event_when_otp_saving_fails() {
        // Given: OTP saving will fail
        doThrow(new RuntimeException("Database save failed")).when(otpSaver).save(any(Otp.class));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When: The use case is executed and fails
        assertThrows(Exception.class, () -> useCase.handle(command));

        // Then: No event should be published
        verify(eventPublisher, never()).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }

    @Test
    void should_not_publish_event_when_any_prerequisite_step_fails() {
        // Scenario: Testing with OTP generation failure as representative case
        // Given: A prerequisite step will fail
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("Step failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

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
        // Given: All operations will succeed
        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

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


// BDD Test Cases: Should throw InitiateVerificationException when any of the steps fail

    @Test
    void should_throw_InitiateVerificationException_when_otp_generation_fails() {
        // Given: OTP generation will fail
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("OTP generation failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_hashing_fails() {
        // Given: Hashing will fail
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_otp_saving_fails() {
        // Given: OTP saving will fail
        doThrow(new RuntimeException("Database save failed")).when(otpSaver).save(any(Otp.class));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_event_publishing_fails() {
        // Given: Event publishing will fail
        doThrow(new RuntimeException("Event publishing failed")).when(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_InitiateVerificationException_when_command_bus_fails() {
        // Given: Command bus will fail
        doThrow(new RuntimeException("Command bus failed")).when(commandBus).sendOtpToUserEmail(anyString(), anyString());

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    // Scenario: Multiple step failures should still throw InitiateVerificationException
    @Test
    void should_throw_InitiateVerificationException_when_multiple_steps_fail() {
        // Given: Multiple operations will fail
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("OTP generation failed"));
        when(hasher.hashSecurely(anyString())).thenThrow(new RuntimeException("Hashing failed"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException
        assertThrows(InitiateVerificationException.class, () -> useCase.handle(command));
    }

    // BDD Test Case: Should include meaningful error message in exception
    @Test
    void should_throw_InitiateVerificationException_with_meaningful_message_when_otp_generation_fails() {
        // Given: OTP generation will fail with specific error
        when(otpGenerator.generate(anyString(), anyInt())).thenThrow(new RuntimeException("Database connection timeout"));

        var command = new InitiateVerificationCommand(VALID_EMAIL);
        var useCase = new InitiateVerificationUseCase(eventPublisher, otpGenerator, hasher, otpSaver, commandBus, DEFAULT_OTP_LEN);

        // When & Then: Should throw InitiateVerificationException with meaningful message
        InitiateVerificationException exception = assertThrows(InitiateVerificationException.class,
                () -> useCase.handle(command));

        assertThat(exception.getMessage()).contains("Failed to initiate verification");
    }
}
// next tests:
// - ???
/*
notes: email will be sent to user even if publishing event `EmailVerificationOtpGeneratedEvent` fails.
 */