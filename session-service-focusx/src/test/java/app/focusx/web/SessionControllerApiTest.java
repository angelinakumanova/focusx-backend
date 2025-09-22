package app.focusx.web;

import app.focusx.security.JwtValidator;
import app.focusx.service.SessionService;
import app.focusx.web.dto.SessionCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
public class SessionControllerApiTest {

    private final static String BASE_URL = "/api/sessions";

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private JwtValidator jwtValidator;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockJwtFilterAuthentication();
    }

    @Test
    void postWithValidBodyRequest_happyPath() throws Exception {
        SessionCreateRequest sessionCreateRequest = new SessionCreateRequest();
        sessionCreateRequest.setUserId(UUID.randomUUID().toString());
        sessionCreateRequest.setMinutes(20);
        sessionCreateRequest.setUserTimezone("Europe/Berlin");

        mockMvc.perform(post(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sessionCreateRequest)))
                .andExpect(status().isCreated());

        verify(sessionService).add(any(SessionCreateRequest.class));
    }

    @Test
    void postWithInvalidBodyRequest_thenReturnsBadRequest() throws Exception {
        SessionCreateRequest sessionCreateRequest = new SessionCreateRequest();
        sessionCreateRequest.setUserId(UUID.randomUUID().toString());
        sessionCreateRequest.setMinutes(0);
        sessionCreateRequest.setUserTimezone("Europe/Berlin");

        mockMvc.perform(post(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sessionCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenUserIdAndTimezone_whenGettingTodaySessionDuration_thenReturnsCorrectValue() throws Exception {
        String userId = UUID.randomUUID().toString();
        String timezone = "Europe/Berlin";
        long expectedDuration = 120L;

        when(sessionService.getTodaysDuration(userId, timezone)).thenReturn(expectedDuration);

        mockMvc.perform(get("/api/sessions/" + userId + "/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                        .with(jwt())
                        .header("User-Timezone", timezone))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedDuration)));

        verify(sessionService).getTodaysDuration(userId, timezone);
    }




    private void mockJwtFilterAuthentication() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(claims.get("role")).thenReturn("ROLE_USER");

        when(jwtValidator.validateToken(any(String.class))).thenReturn(claims);

    }
}
