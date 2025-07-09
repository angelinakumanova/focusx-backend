package app.focusx.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpRateLimiterService {

    private final Map<String, List<LocalDateTime>> ipRegistrationMap = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration TIME_WINDOW = Duration.ofHours(1);

    public boolean isAllowed(String ip) {
        LocalDateTime now = LocalDateTime.now();

        ipRegistrationMap.putIfAbsent(ip, new ArrayList<>());
        List<LocalDateTime> attempts = ipRegistrationMap.get(ip);

        attempts.removeIf(time -> time.isBefore(now.minus(TIME_WINDOW)));

        if (attempts.size() >= MAX_ATTEMPTS) {
            return false;
        }

        attempts.add(now);
        return true;
    }
}
