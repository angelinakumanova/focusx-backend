package app.focusx.messaging.consumer;

import app.focusx.messaging.consumer.event.NewSessionEvent;
import app.focusx.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NewSessionListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public NewSessionListener(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "new-session-event", groupId = "user-service")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            NewSessionEvent event = objectMapper.readValue(record.value(), NewSessionEvent.class);
            if (event.isUpdateStreak()) userService.incrementStreak(event.getUserId());

        } catch (Exception e) {
            log.error("Error handling new session event", e);
        }
    }
}
