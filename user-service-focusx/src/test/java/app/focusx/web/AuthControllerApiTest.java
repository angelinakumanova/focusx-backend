package app.focusx.web;

import app.focusx.MockUtils;
import app.focusx.exception.TooManyAttemptsException;
import app.focusx.exception.UserNotFoundException;
import app.focusx.exception.VerificationException;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.security.JwtService;
import app.focusx.service.IpRateLimiterService;
import app.focusx.service.UserService;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.ResendVerificationRequest;
import app.focusx.web.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerApiTest {

    private static final String BASE_API_URL = "/api/auth";
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private IpRateLimiterService ipRateLimiterService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidRegisterRequest_whenRegister_thenReturnsOk() throws Exception {
        when(ipRateLimiterService.isAllowed(anyString())).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("123456test@A")
                .email("test@example.com").build();

        mockMvc.perform(post(BASE_API_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void givenInvalidRegisterRequest_whenRegister_thenReturnsBadRequest() throws Exception {
        when(ipRateLimiterService.isAllowed(anyString())).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .username("1")
                .email("bad_one")
                .password("123456t").build();

        mockMvc.perform(post(BASE_API_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldLoginSuccessfully() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "secret");
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setRole(UserRole.USER);

        when(userService.login(any(LoginRequest.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(UUID.class), anyString()))
                .thenReturn("access-123");
        when(jwtService.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-123");

        // when + then
        mockMvc.perform(post(BASE_API_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-123"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString("refresh_token=refresh-123")));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsInvalid() throws Exception {
        // given
        LoginRequest request = new LoginRequest("wrong@example.com", "wrongpass");
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // when + then
        mockMvc.perform(post(BASE_API_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenReturnsNewAccessTokenCookie() throws Exception {
        String refreshToken = "valid-refresh-token";
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId.toString())
                .role(UserRole.USER)
                .build();

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId.toString());
        when(userService.getById(userId)).thenReturn(user);
        when(jwtService.generateAccessToken(userId, user.getRole().toString())).thenReturn("new-access-token");

        mockMvc.perform(post(BASE_API_URL + "/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token"));

        verify(jwtService).extractUserId(refreshToken);
        verify(userService).getById(userId);
        verify(jwtService).generateAccessToken(userId, user.getRole().toString());
    }

    @Test
    void givenMissingRefreshToken_whenRefresh_thenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_API_URL + "/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenValidAccessToken_whenGetMe_thenReturnsUserInfo() throws Exception {
        MockUtils.mockJwtFilterAuthentication(userService, jwtService);
        String accessToken = "valid-token";
        UUID userId = UUID.randomUUID();

        UserResponse expectedUser = UserResponse.builder()
                .id(userId.toString())
                .username("testuser").build();

        when(jwtService.extractUserId(accessToken)).thenReturn(userId.toString());
        when(userService.getInfo(userId)).thenReturn(expectedUser);

        mockMvc.perform(get(BASE_API_URL + "/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getInfo(userId);
    }

    @Test
    void givenNoAccessToken_whenGetMe_thenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_API_URL + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldClearRefreshTokenCookie() throws Exception {
        MockUtils.mockJwtFilterAuthentication(userService, jwtService);

        mockMvc.perform(get(BASE_API_URL + "/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer some-access-token"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.allOf(
                                Matchers.containsString("refresh_token="),
                                Matchers.containsString("Max-Age=0")
                        ))
                ));
    }

    @Test
    void verify_shouldReturn200_whenCodeValid() throws Exception {
        // given
        String validCode = "abc123";

        // when + then
        mockMvc.perform(post(BASE_API_URL + "/verify")
                        .param("verificationCode", validCode))
                .andExpect(status().isOk());

        // verify controller calls service
        verify(userService).verify(validCode);
    }

    @Test
    void verify_shouldReturn400_whenCodeInvalid() throws Exception {
        String invalidCode = "wrong-code";

        doThrow(new VerificationException("Invalid or expired verification code"))
                .when(userService).verify(invalidCode);

        mockMvc.perform(post(BASE_API_URL + "/verify")
                        .param("verificationCode", invalidCode))
                .andExpect(status().isBadRequest());

        verify(userService).verify(invalidCode);
    }

    @Test
    void resendVerification_shouldReturn200_whenValidRequestData() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest("test@example.com");

        mockMvc.perform(post(BASE_API_URL + "/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).resendVerification(request.getEmail());
    }

    @Test
    void resendVerification_shouldReturnBadRequest_forTooManyAttempts() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest("test@example.com");

        doThrow(new TooManyAttemptsException("Too many attempts")).when(userService).resendVerification(request.getEmail());

        mockMvc.perform(post(BASE_API_URL + "/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void resendVerification_shouldReturnNotFound_forUnknownUser() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest("unknown@test.com");

        doThrow(new UserNotFoundException("unknown@test.com"))
                .when(userService).resendVerification(request.getEmail());

        mockMvc.perform(post(BASE_API_URL + "/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }



}
