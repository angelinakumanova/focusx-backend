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
import java.util.Map;
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

    @PostMapping("/verify")
    @Operation(
            summary = "Verify user",
            description = "Verifies a user by verification code sent on email and sets access and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verification successful, tokens set."),
            }
    )
    public ResponseEntity<?> verify(@RequestParam String code) {
        User user = userService.verify(code);

        return buildAuthResponse(user);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate a user",
            description = "Verifies user credentials and sets access and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful, tokens set"),
                    @ApiResponse(responseCode = "401", description = "Invalid username or password")
            }
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.login(request);

        return buildAuthResponse(user);
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

                return ResponseEntity.ok()
                        .body(Map.of("access_token", newAccessToken));
            }
        }


        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user info",
            description = "Returns user info based on access token in authorization header.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Access token is missing or invalid")
            }
    )
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String accessToken) {

        if (accessToken != null) {
            String token = accessToken.substring("Bearer ".length());
            String userId = jwtService.extractUserId(token);

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

    private ResponseEntity<Map<String, String>> buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(UUID.fromString(user.getId()), user.getRole().toString());
        String refreshToken = jwtService.generateRefreshToken(UUID.fromString(user.getId()), user.getRole().toString());


        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("None")
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("access_token", accessToken));
    }
}
