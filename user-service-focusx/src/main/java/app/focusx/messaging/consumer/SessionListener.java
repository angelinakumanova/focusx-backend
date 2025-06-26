package app.focusx.messaging.consumer;

import app.focusx.messaging.event.SessionEvent;
import app.focusx.service.UserService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SessionListener {

    private final UserService userService;

    public SessionListener(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(topics = "session-events", groupId = "session-service")
    public void listen(SessionEvent sessionEvent) {
        userService.incrementStreak(sessionEvent.getUserId());
    }
}
