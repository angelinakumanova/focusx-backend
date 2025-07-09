package app.focusx.scheduler;

import app.focusx.service.SessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OldSessionsCleanUp {

    private final SessionService sessionService;

    public OldSessionsCleanUp(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldSessions() {
        sessionService.cleanOldSessionsOlderThan1Day();
    }
}
