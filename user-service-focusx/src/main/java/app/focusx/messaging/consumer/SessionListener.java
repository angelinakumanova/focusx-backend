package app.focusx.messaging.consumer;

import app.focusx.messaging.consumer.event.SessionEvent;
import app.focusx.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SessionListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public SessionListener(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "session-events", groupId = "user-service")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            SessionEvent event = objectMapper.readValue(record.value(), SessionEvent.class);
            if (event.isUpdateStreak()) userService.incrementStreak(event.getUserId());

        } catch (JsonProcessingException e) {
            log.error("Error parsing session event", e);
        }
    }
}
