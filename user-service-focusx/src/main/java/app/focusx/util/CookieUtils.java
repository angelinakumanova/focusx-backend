package app.focusx.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {
    private static final String DOMAIN_NAME = ".up.railway.app";

    public static void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("access_token") || cookie.getName().equals("refresh_token")) {
                Cookie cleared = new Cookie(cookie.getName(), null);
                cleared.setPath(cookie.getPath() != null ? cookie.getPath() : "/");
                cleared.setHttpOnly(cookie.isHttpOnly());
                cleared.setSecure(cookie.getSecure());
                cleared.setMaxAge(0);

                if (cookie.getDomain() != null) {
                    cleared.setDomain(cookie.getDomain());
                }

                response.addCookie(cleared);
            }
        }
    }


    public static ResponseCookie buildResponseCookie(
            String name,
            String value,
            Duration maxAge
    ) {
        return ResponseCookie.from(name, value)
                .domain(DOMAIN_NAME)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("None")
                .secure(true)
                .build();
    }
}
