package bankapp.auth.rest.verification;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification_complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.application.verification_initiate.InitiateVerificationUseCase;
import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.rest.exception.GlobalExceptionHandler;
import bankapp.auth.rest.verification.dto.CompleteVerificationRequest;
import bankapp.auth.rest.verification.dto.InitiateVerificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(classes = { VerificationController.class, GlobalExceptionHandler.class })
class VerificationControllerTest {

    public static final String VERIFICATION_INITIATE_ENDPOINT = "/verification/initiate/email/";
    public static final String VERIFICATION_COMPLETE_ENDPOINT = "/verification/complete/email/";

    @Autowired
    private WebTestClient webTestClient; // The main tool for testing

    @MockitoBean
    private InitiateVerificationUseCase initiateVerificationUseCase;

    @MockitoBean
    private CompleteVerificationUseCase completeVerificationUseCase;

    @Test
    void should_return_202_when_provided_valid_email() {
        var command = new InitiateVerificationRequest("test@bankapp.online");

        // Use doNothing() for void-returning methods
        doNothing().when(initiateVerificationUseCase).handle(any(InitiateVerificationCommand.class));

        // Act & Assert
        webTestClient.post().uri(VERIFICATION_INITIATE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody().isEmpty();

        verify(initiateVerificationUseCase, timeout(1000)).handle(any(InitiateVerificationCommand.class));
    }

    @Test
    void should_return_4XX_when_provided_invalid_email() {
        var command = new InitiateVerificationRequest("test-bankapp.online");

        // Use doNothing() for void-returning methods
        doNothing().when(initiateVerificationUseCase).handle(any(InitiateVerificationCommand.class));

        // Act & Assert
        webTestClient.post().uri(VERIFICATION_INITIATE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange() // Executes the request
                .expectStatus().is4xxClientError()
                .expectBody(); // Verify the body is not empty

        verifyNoInteractions(initiateVerificationUseCase);
    }

    @Test
    void completeEmailVerification_whenSuccessful_shouldReturnResponseAnd200() {
        // Arrange
        var request = new CompleteVerificationRequest("test@bankapp.online","123123");
        UUID challengeId = UUID.randomUUID();
        // Let's assume the use case returns a LoginResponse
        LoginResponse mockResponse = new LoginResponse(
                new PublicKeyCredentialRequestOptions(
                        new byte[]{123},
                        50L,
                        "bankapp.online",
                        null,
                        null,
                        null
                ), challengeId);

        // Mock the use case to return our object
        when(completeVerificationUseCase.handle(any(CompleteVerificationCommand.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        webTestClient.post().uri(VERIFICATION_COMPLETE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Assert HTTP 200 OK
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Use JsonPath to assert specific fields in the response body
                .expectBody()
                .jsonPath("$.type").isEqualTo("login") // If using Jackson annotations
                .jsonPath("$.challengeId").isEqualTo(challengeId.toString())
                .jsonPath("$.options").exists();
    }
}