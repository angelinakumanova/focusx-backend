package app.focusx.filter;

import app.focusx.service.IpRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final IpRateLimiterService service;

    public IpRateLimitFilter(IpRateLimiterService service) {
        this.service = service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if ("/auth/register".equals(path)) {
            String ip = request.getRemoteAddr();

            if (!service.isAllowed(ip)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many registration attempts. Please try again later.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
