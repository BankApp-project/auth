package bankapp.auth.infrastructure.rest.authentication;

import bankapp.auth.application.authentication_complete.CompleteAuthenticationCommand;
import bankapp.auth.application.authentication_complete.CompleteAuthenticationUseCase;
import bankapp.auth.application.authentication_initiate.InitiateAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.infrastructure.rest.shared.dto.AuthenticationGrantResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @MockitoBean
    InitiateAuthenticationUseCase initiateAuthenticationUseCase;

    @MockitoBean
    CompleteAuthenticationUseCase completeAuthenticationUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_return_response_with_challengeId_and_PublicKeyCredentialRequestOptions() throws Exception {

        var options = new PublicKeyCredentialRequestOptions(
                new byte[]{123},
                null,
                null,
                null,
                null,
                null
        );
        var loginResponse = new LoginResponse(options, UUID.randomUUID());

        when(initiateAuthenticationUseCase.handle()).thenReturn(loginResponse);

        var responseStub = new InitiateAuthenticationResponse(options, loginResponse.challengeId().toString());
        var responseJson = objectMapper.writeValueAsString(responseStub);


        mockMvc.perform(get("/authentication/initiate")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }

    @Test
    void should_return_valid_response_with_valid_request() throws Exception {

        var challengeId = UUID.randomUUID().toString();
        var authRespJson = "someJSONblob";
        var credentialId = UUID.randomUUID();
        var request = new CompleteAuthenticationRequest(challengeId, authRespJson, credentialId);
        var requestJson = objectMapper.writeValueAsString(request);

        var requestedCommand = new CompleteAuthenticationCommand(UUID.fromString(challengeId), authRespJson, credentialId);

        var authTokens = new AuthTokens("accessToken", "refreshToken");
        var useCaseResponse = new AuthenticationGrant(authTokens);

        when(completeAuthenticationUseCase.handle(any())).thenReturn(useCaseResponse);

        var responseWithTokens = new AuthenticationGrantResponse("accessToken", "refreshToken");
        var responseJson = objectMapper.writeValueAsString(responseWithTokens);

        mockMvc.perform(post("/authentication/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

        verify(completeAuthenticationUseCase).handle(argThat(command -> {
            boolean first = command.AuthenticationResponseJSON().equals(requestedCommand.AuthenticationResponseJSON());
            boolean second = command.challengeId().equals(requestedCommand.challengeId());
            boolean third = Objects.equals(command.credentialId(),requestedCommand.credentialId());

            return first && second && third;
        }));
    }
}