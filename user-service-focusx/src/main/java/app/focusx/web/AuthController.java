package app.focusx.web;

import app.focusx.model.User;
import app.focusx.security.JwtService;
import app.focusx.service.UserService;
import app.focusx.util.CookieUtils;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
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
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request) {
        this.userService.register(request);
    }

    @PostMapping("/login")
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
    public ResponseEntity<?> me(@CookieValue(value = "access_token") String accessToken) {


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

    @GetMapping
    public void logout(HttpServletResponse response) {
        CookieUtils.clearAuthCookies(response);
    }
}
