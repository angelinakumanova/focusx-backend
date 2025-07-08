package app.focusx.web;

import app.focusx.model.User;
import app.focusx.security.JwtService;
import app.focusx.service.UserService;
import app.focusx.util.CookieUtils;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token handling")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Takes a username, email, and password to create an account.")
    public void register(@Valid @RequestBody RegisterRequest request) {
        this.userService.register(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate a user",
            description = "Verifies user credentials and sets access and refresh token cookies.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful, cookies set"),
                    @ApiResponse(responseCode = "401", description = "Invalid username or password")
            }
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.verify(request);

        String accessToken = jwtService.generateAccessToken(UUID.fromString(user.getId()), user.getRole().toString());
        String refreshToken = jwtService.generateRefreshToken(UUID.fromString(user.getId()), user.getRole().toString());


        ResponseCookie accessTokenCookie = CookieUtils.buildResponseCookie("access_token", accessToken, Duration.ofMinutes(15));
        ResponseCookie refreshTokenCookie = CookieUtils.buildResponseCookie("refresh_token", refreshToken, Duration.ofDays(7));


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Uses the refresh token from cookies to generate a new access token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
                    @ApiResponse(responseCode = "401", description = "Refresh token is missing or invalid")
            }
    )
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if (refreshToken != null) {
            String userId = jwtService.extractUserId(refreshToken);

            if (userId != null) {
                User user = userService.getById(UUID.fromString(userId));

                String newAccessToken = jwtService.generateAccessToken(UUID.fromString(user.getId()), user.getRole().toString());

                ResponseCookie accessCookie = CookieUtils.buildResponseCookie("access_token", newAccessToken, Duration.ofMinutes(15));

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .build();
            }
        }


        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user info",
            description = "Returns user info based on access token in cookie.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Access token is missing or invalid")
            }
    )
    public ResponseEntity<?> me(@CookieValue(value = "access_token", required = false) String accessToken) {

        if (accessToken != null) {
            String userId = jwtService.extractUserId(accessToken);

            if (userId != null) {
                UserResponse userResponse = userService.getInfo(UUID.fromString(userId));
                return ResponseEntity.ok()
                        .body(userResponse);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/logout")
    @Operation(
            summary = "Log out the user",
            description = "Clears authentication cookies.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged out successfully")
            }
    )
    public void logout(HttpServletResponse response) {
        CookieUtils.clearAuthCookies(response);
    }
}
