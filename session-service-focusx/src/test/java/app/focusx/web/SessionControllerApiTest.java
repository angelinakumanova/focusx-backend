package app.focusx.web;

import app.focusx.security.JwtValidator;
import app.focusx.service.SessionService;
import app.focusx.web.dto.SessionCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SessionControllerApiTest {

    private final static String BASE_URL = "/api/sessions";

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private JwtValidator jwtValidator;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidSessionCreateRequest_whenCreateSession_thenReturnsOk() throws Exception {
        mockJwtFilterAuthentication();

        SessionCreateRequest sessionCreateRequest = new SessionCreateRequest();
        sessionCreateRequest.setUserId(UUID.randomUUID().toString());
        sessionCreateRequest.setMinutes(20);
        sessionCreateRequest.setUserTimezone("Europe/Berlin");

        mockMvc.perform(post(BASE_URL)
                        .cookie(new Cookie("access_token", "fake-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sessionCreateRequest)))
                .andExpect(status().isCreated());

        verify(sessionService).add(any(SessionCreateRequest.class));
    }

    @Test
    void givenInvalidSessionCreateRequest_whenCreateSession_thenReturnsBadRequest() throws Exception {
        mockJwtFilterAuthentication();

        SessionCreateRequest sessionCreateRequest = new SessionCreateRequest();
        sessionCreateRequest.setUserId(UUID.randomUUID().toString());
        sessionCreateRequest.setMinutes(0);
        sessionCreateRequest.setUserTimezone("Europe/Berlin");

        mockMvc.perform(post(BASE_URL)
                        .cookie(new Cookie("access_token", "fake-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sessionCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenUserIdAndTimezone_whenGetTodaySessionsDuration_thenReturnsOk() throws Exception {
        mockJwtFilterAuthentication();

        String userId = UUID.randomUUID().toString();
        String timezone = "Europe/Berlin";
        long expectedDuration = 120L;

        when(sessionService.getTodaysDuration(userId, timezone)).thenReturn(expectedDuration);

        mockMvc.perform(get("/api/sessions/" + userId + "/today")
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
