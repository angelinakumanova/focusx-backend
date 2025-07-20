package app.focusx.service;

import app.focusx.messaging.producer.SessionEventProducer;
import app.focusx.model.Session;
import app.focusx.repository.SessionRepository;
import app.focusx.web.dto.SessionCreateRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionEventProducer producer;

    private final RedisTemplate<String, String> redisTemplate;

    public SessionService(SessionRepository sessionRepository, SessionEventProducer producer, RedisTemplate<String, String> redisTemplate) {
        this.sessionRepository = sessionRepository;
        this.producer = producer;
        this.redisTemplate = redisTemplate;
    }

    @CacheEvict(value ="duration", key = "#request.userId")
    public void add(SessionCreateRequest request) {
        sendNewSessionEvent(request.getUserId(), request.getMinutes(), request.getUserTimezone());
        sessionRepository.save(initializeSession(request));
    }

    public long getTodaysDuration(String userId, String userTimeZone) {
        String value = redisTemplate.opsForValue().get("duration::" + userId);

        if (value != null) {
            return Long.parseLong(value);
        }


        Long duration = getTodaysSessions(userId, userTimeZone)
                .stream()
                .map(Session::getMinutes)
                .reduce(0L, Long::sum);
        cacheWithCustomTTL(userId, duration, userTimeZone);

        return duration;
    }

    private void cacheWithCustomTTL(String userId, Long duration, String timezone) {
        ZoneId zone = ZoneId.of(timezone);

        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        long diff = Duration.between(now, nextMidnight).toMillis();
        String key = "duration::" + userId;

        redisTemplate.opsForValue().set(key, duration.toString(), diff, TimeUnit.MILLISECONDS);
    }

    private void sendNewSessionEvent(String userId, long minutes, String timezone) {
        boolean updateStreak = getTodaysSessions(userId, timezone).isEmpty();

        producer.sendNewSessionAddedEvent(userId, minutes, updateStreak);
    }

    private List<Session> getTodaysSessions(String userId, String userTimeZone) {
        ZoneId userZone = ZoneId.of(userTimeZone);

        ZonedDateTime startOfDay = LocalDate.now(userZone).atStartOfDay(userZone);
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        Instant utcStart = startOfDay.toInstant();
        Instant utcEnd = endOfDay.toInstant();

        return sessionRepository.findByCompletedAtBetweenAndUserId(utcStart, utcEnd, userId);
    }

    private Session initializeSession(SessionCreateRequest request) {
        Instant now = Instant.now();

        return Session.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .minutes(request.getMinutes())
                .completedAt(now)
                .build();
    }

    public void cleanOldSessionsOlderThan1Day() {
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        sessionRepository.deleteByCompletedAtBefore(oneDayAgo);
    }
}
