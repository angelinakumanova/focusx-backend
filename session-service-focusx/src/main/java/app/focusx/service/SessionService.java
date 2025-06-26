package app.focusx.service;

import app.focusx.messaging.producer.SessionEventProducer;
import app.focusx.model.Session;
import app.focusx.repository.SessionRepository;
import app.focusx.web.dto.SessionCreateRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionEventProducer producer;

    public SessionService(SessionRepository sessionRepository, SessionEventProducer producer) {
        this.sessionRepository = sessionRepository;
        this.producer = producer;
    }

    @CacheEvict(value ="duration", key = "#request.userId")
    public void add(SessionCreateRequest request) {
        sendNewSessionEvent(request.getUserId(), request.getMinutes(), request.getUserTimezone());
        sessionRepository.save(initializeSession(request));
    }

    @Cacheable(value = "duration", key = "#userId")
    public long getTodaysDuration(String userId, String userTimeZone) {
        return getTodaysSessions(userId, userTimeZone)
                .stream()
                .map(Session::getMinutes)
                .reduce(0L, Long::sum);
    }

    private void sendNewSessionEvent(String userId, long minutes, String timezone) {


        if (!getTodaysSessions(userId, timezone).isEmpty()) {
            return;
        }

        producer.sendNewSessionAddedEvent(userId, minutes);
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

}
