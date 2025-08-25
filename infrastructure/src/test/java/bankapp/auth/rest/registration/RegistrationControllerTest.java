package bankapp.auth.rest.registration;

import bankapp.auth.application.registration_complete.CompleteRegistrationUseCase;
import bankapp.auth.application.registration_complete.port.in.CompleteRegistrationCommand;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    public static final String DEFAULT_REGISTRATION_RESPONSE = "regResp";
    public static final AuthTokens DEFAULT_AUTH_TOKENS = new AuthTokens("accessToken", "refreshToken");
    public static final UUID DEFAULT_CHALLENGE_ID = UUID.randomUUID();
    private static final URI REGISTRATION_COMPLETE_ENDPOINT = URI.create("/registration/complete");

    private CompleteRegistrationRequest request;
    private CompleteRegistrationCommand command;
    private AuthenticationGrantResponse response;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompleteRegistrationUseCase completeRegistrationUseCase;

    @Test
    void should_return_authTokens_and_OK_when_valid_challengeId_and_registrationResponse() throws Exception {

        request = new CompleteRegistrationRequest(DEFAULT_CHALLENGE_ID.toString(), DEFAULT_REGISTRATION_RESPONSE);
        command = new CompleteRegistrationCommand(DEFAULT_CHALLENGE_ID, DEFAULT_REGISTRATION_RESPONSE);
        response = new AuthenticationGrantResponse(DEFAULT_AUTH_TOKENS.accessToken(), DEFAULT_AUTH_TOKENS.refreshToken());

        var authenticationGrantStub = new AuthenticationGrant(DEFAULT_AUTH_TOKENS);

        String jsonRequest = objectMapper.writeValueAsString(request);
        String jsonResponse = objectMapper.writeValueAsString(response);

        when(completeRegistrationUseCase.handle(command)).thenReturn(authenticationGrantStub);

        mockMvc.perform(post(REGISTRATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

    }

    @Test
    void should_return_400_when_invalid_challengeId() throws Exception {
        var invalidChallengeId = "invalidChallengeId123";
        request = new CompleteRegistrationRequest(invalidChallengeId, DEFAULT_REGISTRATION_RESPONSE);

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(REGISTRATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

    }
}