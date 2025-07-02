package app.focusx.web;

import app.focusx.MockUtils;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.security.JwtService;
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

import static org.mockito.ArgumentMatchers.any;
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

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidRegisterRequest_whenRegister_thenReturnsOk() throws Exception {
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
    void givenValidLoginCredentials_whenLogin_thenReturnsOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456test@A");

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .role(UserRole.USER)
                .build();

        when(userService.verify(request)).thenReturn(user);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("fake-access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("fake-refresh-token");

        mockMvc.perform(post(BASE_API_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie",
                        org.hamcrest.Matchers.hasItems(
                                org.hamcrest.Matchers.startsWith("access_token="),
                                org.hamcrest.Matchers.startsWith("refresh_token=")
                        )));

        verify(userService).verify(request);
        verify(jwtService).generateAccessToken(any(), any());
        verify(jwtService).generateRefreshToken(any(), any());
    }

    @Test
    void givenInvalidLoginCredentials_whenLogin_thenReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("1");
        request.setPassword("123456t");

        when(userService.verify(request)).thenThrow(new BadCredentialsException("Invalid credentials"));

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
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("access_token=")));

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
                                Matchers.containsString("access_token="),
                                Matchers.containsString("Max-Age=0")
                        ))
                ))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.allOf(
                                Matchers.containsString("refresh_token="),
                                Matchers.containsString("Max-Age=0")
                        ))
                ));
    }



}
