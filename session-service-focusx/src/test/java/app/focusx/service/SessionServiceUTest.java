package app.focusx.service;

import app.focusx.messaging.producer.SessionEventProducer;
import app.focusx.model.Session;
import app.focusx.repository.SessionRepository;
import app.focusx.web.dto.SessionCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceUTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionEventProducer producer;

    @Captor
    ArgumentCaptor<Session> sessionCaptor;


    @Test
    void givenHappyPath_whenAdd() {
        UUID userId = UUID.randomUUID();
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId(userId.toString());
        request.setMinutes(20);
        request.setUserTimezone("Europe/Bucharest");

        sessionService.add(request);

        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        Session session = sessionCaptor.getValue();

        verify(producer, times(1)).sendNewSessionAddedEvent(any(), any(Long.class), any(boolean.class));
        assertThat(session.getMinutes()).isEqualTo(request.getMinutes());
    }

    @Test
    void givenExistingSessionsOfToday_WhenGetTodaysDuration_thenReturnTotalDuration() {
        UUID userId = UUID.randomUUID();

        Session session1 = Session.builder()
                .minutes(10)
                .completedAt(Instant.now())
                .userId(userId.toString()).build();
        Session session2 = Session.builder()
                .minutes(20)
                .completedAt(Instant.now())
                .userId(userId.toString()).build();
        List<Session> sessions = List.of(session1, session2);

        when(sessionRepository
                .findByCompletedAtBetweenAndUserId(any(Instant.class), any(Instant.class), eq(userId.toString())))
                .thenReturn(sessions);

        long todaysDuration = sessionService.getTodaysDuration(userId.toString(), "Europe/Bucharest");

        assertThat(todaysDuration).isEqualTo(30);
    }
}
