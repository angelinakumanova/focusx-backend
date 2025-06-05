package app.focusx.service;

import app.focusx.model.Session;
import app.focusx.repository.SessionRepository;
import app.focusx.web.dto.SessionCreateRequest;
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

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void add(SessionCreateRequest request) {
        sessionRepository.save(initializeSession(request));
    }

    public long getTodaysDuration(String userId, String userTimeZone) {
        ZoneId userZone = ZoneId.of(userTimeZone);

        ZonedDateTime startOfDay = LocalDate.now(userZone).atStartOfDay(userZone);
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        Instant utcStart = startOfDay.toInstant();
        Instant utcEnd = endOfDay.toInstant();

        List<Session> todaysSessions = sessionRepository.findByCompletedAtBetween(utcStart, utcEnd);
        return todaysSessions.stream().map(Session::getMinutes).reduce(0L, Long::sum);
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
