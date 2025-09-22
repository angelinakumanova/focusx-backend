package app.focusx.web;

import app.focusx.MockUtils;
import app.focusx.security.JwtService;
import app.focusx.service.IpRateLimiterService;
import app.focusx.service.UserService;
import app.focusx.util.CookieUtils;
import app.focusx.web.dto.PasswordUpdateRequest;
import app.focusx.web.dto.UsernameUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {
    private final static String BASE_API_URL = "/api/users";

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private IpRateLimiterService ipRateLimiterService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockUtils.mockJwtFilterAuthentication(userService, jwtService);
    }

    @Test
    void givenValidIdAndUsername_whenUpdateUsername_thenReturnsOk() throws Exception {
        String id = UUID.randomUUID().toString();
        String newUsername = "testuser";

        doNothing().when(userService).updateUsername(id, newUsername);

        mockMvc.perform(put(BASE_API_URL + "/" + id + "/username")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(
                                new UsernameUpdateRequest(newUsername)
                        )))
                .andExpect(status().isOk());

        verify(userService).updateUsername(id, newUsername);
    }

    @Test
    void givenValidIdAndPasswordRequest_whenUpdatePassword_thenReturnsOk() throws Exception {
        String id = UUID.randomUUID().toString();
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("123456test@A");

        doNothing().when(userService).updatePassword(id, request.getCurrentPassword(), request.getNewPassword());

        mockMvc.perform(put(BASE_API_URL + "/" + id + "/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updatePassword(id, request.getCurrentPassword(), request.getNewPassword());
    }

    @Test
    void givenValidId_whenDeleteAccount_thenReturnsOkAndClearsCookies() throws Exception {
        String id = UUID.randomUUID().toString();

        doNothing().when(userService).deactivate(id);

        try (MockedStatic<CookieUtils> cookieUtilsMock = Mockito.mockStatic(CookieUtils.class)) {
            cookieUtilsMock.when(() -> CookieUtils.clearAuthCookies(any(HttpServletResponse.class))).thenAnswer(invocation -> null);

            mockMvc.perform(put(BASE_API_URL + "/" + id + "/deactivate")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                    .andExpect(status().isOk());

            verify(userService).deactivate(id);
            cookieUtilsMock.verify(() -> CookieUtils.clearAuthCookies(any(HttpServletResponse.class)), times(1));
        }
    }

    @Test
    void givenValidIdAndTimezone_whenGetStreak_thenReturnsValue() throws Exception {
        String id = UUID.randomUUID().toString();
        String timezone = "Europe/Berlin";
        long streakValue = 5L;

        when(userService.getStreak(id, timezone)).thenReturn(streakValue);

        mockMvc.perform(get(BASE_API_URL + "/" + id + "/streak")
                        .header("timezone", timezone)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(streakValue)));

        verify(userService).getStreak(id, timezone);
    }
}
