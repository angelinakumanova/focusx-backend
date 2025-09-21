package app.focusx.web;

import app.focusx.MockUtils;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.security.JwtService;
import app.focusx.service.IpRateLimiterService;
import app.focusx.service.UserService;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
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

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    void shouldLoginSuccessfully() throws Exception {
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
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-123"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString("refresh_token=refresh-123")));
    }

    @Test
    void givenInvalidLoginCredentials_whenLogin_thenReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("1");
        request.setPassword("123456t");

        when(userService.login(request)).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post(BASE_API_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
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
                .andExpect(header().string("Set-Cookie", containsString("access_token=")));

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
                        .cookie(new Cookie("access_token", accessToken)))
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
    void givenLogout_whenCalled_thenClearsAuthCookies() throws Exception {
        MockUtils.mockJwtFilterAuthentication(userService, jwtService);

        Cookie accessTokenCookie = new Cookie("access_token", "some-access-token-value");
        Cookie refreshTokenCookie = new Cookie("refresh_token", "some-refresh-token-value");

        mockMvc.perform(get(BASE_API_URL + "/logout")
                        .cookie(accessTokenCookie).cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.allOf(
                                containsString("access_token="),
                                containsString("Max-Age=0")
                        ))
                ))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.allOf(
                                containsString("refresh_token="),
                                containsString("Max-Age=0")
                        ))
                ));
    }



}
