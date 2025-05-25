package app.focusx.web;

import app.focusx.security.JwtService;
import app.focusx.service.UserService;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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
    public void register(@RequestBody RegisterRequest request) {
        this.userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String userId = userService.verify(request);

        String accessToken = jwtService.generateAccessToken(userId);
        String refreshToken = jwtService.generateRefreshToken(userId);

        // TODO: Set secure to "true" in production
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if (refreshToken != null) {
            String userId = jwtService.extractUserId(refreshToken);

            if (userId != null) {

                String newAccessToken = jwtService.generateAccessToken(userId);

                ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                        .httpOnly(true)
                        .path("/")
                        .maxAge(Duration.ofMinutes(15))
                        .sameSite("Strict")
                        .build();

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
                UserResponse userResponse = userService.getInfo(userId);
                return ResponseEntity.ok()
                        .body(userResponse);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
