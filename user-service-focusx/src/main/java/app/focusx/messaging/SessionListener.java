package app.focusx.messaging;

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
    public void listen(String userId) {
        userService.incrementStreak(userId);
    }
}
