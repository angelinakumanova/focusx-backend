package app.focusx.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class IpRateLimiterServiceUTest {
    @InjectMocks
    private IpRateLimiterService service;

    @Test
    void givenIpWithLessThanMaxAttempts_whenCheck_thenAllowed() {
        String ip = "192.168.0.1";

        service.isAllowed(ip);
        service.isAllowed(ip);

        boolean allowed = service.isAllowed(ip);

        assertTrue(allowed);
    }

    @Test
    void givenIpWithMaxAttempts_whenCheck_thenDenied() {
        String ip = "10.0.0.1";

        service.isAllowed(ip);
        service.isAllowed(ip);
        service.isAllowed(ip);

        boolean allowed = service.isAllowed(ip);

        assertFalse(allowed);
    }

}
