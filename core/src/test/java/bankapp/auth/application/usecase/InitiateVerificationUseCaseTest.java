package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.EventPublisher;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InitiateVerificationUseCaseTest {

    private static final EventPublisher eventPublisher = mock(EventPublisher.class);

    private static final int OTP_SIZE = 6;
    //    Test Case 1: Successful Verification Initiation
    //    Given: A user is on the login page
    //    When: A user provides email to `provide email` form and clicks `continue` button
    //    Then: User gets email msg containing otp

    //    Test Case 1: Successful Verification Initiation
    //    Given: A user is on the login page
    //    When: A user provides their email and clicks 'continue'
    //    Then: An "verification-otp-generated" event should be published
    //    And: The event should contain the user's email
    //    And: The event should contain a valid one-time password
    //    And: The otp should contain 6 digit long code.

    @Test
    void should_publish_event_when_provided_valid_email() {
        //given
        String validEmail = "test@bankapp.online";

        //when
        var command = new InitiateVerificationCommand(validEmail, OTP_SIZE);
        var useCase = new InitiateVerificationUseCase(eventPublisher);
        useCase.handle(command);

        //then
       verify(eventPublisher).publish(any(EmailVerificationOtpGeneratedEvent.class));
    }
}
