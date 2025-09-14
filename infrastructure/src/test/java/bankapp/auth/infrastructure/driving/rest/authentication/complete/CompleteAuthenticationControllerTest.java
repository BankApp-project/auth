package bankapp.auth.infrastructure.driving.rest.authentication.complete;

import bankapp.auth.application.authentication.complete.CompleteAuthenticationCommand;
import bankapp.auth.application.authentication.complete.CompleteAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.infrastructure.driving.rest.shared.dto.AuthenticationGrantResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(CompleteAuthenticationController.class)
class CompleteAuthenticationControllerTest {

    @MockitoBean
    private CompleteAuthenticationUseCase completeAuthenticationUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_return_valid_response_with_valid_request() throws Exception {
        // Given
        var sessionId = UUID.randomUUID().toString();
        var authRespJson = "someJSONblob";
        var credentialId = UUID.randomUUID();
        var request = new CompleteAuthenticationRequest(sessionId, authRespJson, credentialId);
        var requestJson = objectMapper.writeValueAsString(request);

        var requestedCommand = new CompleteAuthenticationCommand(UUID.fromString(sessionId), authRespJson, credentialId);

        var authTokens = new AuthTokens("accessToken", "refreshToken");
        var useCaseResponse = new AuthenticationGrant(authTokens);

        when(completeAuthenticationUseCase.handle(any(CompleteAuthenticationCommand.class))).thenReturn(useCaseResponse);

        var responseWithTokens = new AuthenticationGrantResponse("accessToken", "refreshToken");
        var responseJson = objectMapper.writeValueAsString(responseWithTokens);

        // When & Then
        mockMvc.perform(post("/authentication/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

        // Verify that the use case was called with the correct arguments
        verify(completeAuthenticationUseCase).handle(argThat(command -> {
            boolean sessionIdMatches = command.sessionId().equals(requestedCommand.sessionId());
            boolean responseJsonMatches = command.AuthenticationResponseJSON().equals(requestedCommand.AuthenticationResponseJSON());
            boolean credentialIdMatches = Objects.equals(command.credentialId(), requestedCommand.credentialId());

            return sessionIdMatches && responseJsonMatches && credentialIdMatches;
        }));
    }
}
