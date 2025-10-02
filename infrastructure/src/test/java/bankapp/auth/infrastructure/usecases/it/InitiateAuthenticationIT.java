package bankapp.auth.infrastructure.usecases.it;


import bankapp.auth.application.shared.port.out.repository.SessionRepository;
import bankapp.auth.infrastructure.driving.rest.authentication.initiate.InitiateAuthenticationResponse;
import bankapp.auth.infrastructure.utils.WithRedisContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Transactional // Automatically rolls back database changes after each test
public class InitiateAuthenticationIT implements WithRedisContainer {

    private static final String INITIATE_AUTHENTICATION_ENDPOINT = "/authentication/initiate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void should_return_loginResponse() throws Exception {
        var res = mockMvc.perform(get(INITIATE_AUTHENTICATION_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.loginOptions.challenge").isNotEmpty())
                .andExpect(jsonPath("$.loginOptions.timeout").isNumber())
                .andExpect(jsonPath("$.loginOptions.rpId").isString())
                .andReturn();

        assertChallengePersisted(res);
    }

    private void assertChallengePersisted(MvcResult res) throws UnsupportedEncodingException, JsonProcessingException {
        var jsonBody = res.getResponse().getContentAsString();
        InitiateAuthenticationResponse responseObj = objectMapper.readValue(jsonBody,InitiateAuthenticationResponse.class);

        var sessionId = responseObj.sessionId();
        var sessionUUID = UUID.fromString(sessionId);
        var loadedChallenge = sessionRepository.load(sessionUUID);

        assertThat(loadedChallenge).isPresent();
    }
}
