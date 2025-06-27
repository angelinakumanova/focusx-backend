package app.focusx.messaging.producer;

import app.focusx.messaging.event.SessionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SessionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SessionEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendNewSessionAddedEvent(String userId, long minutes, boolean updateStreak) {
        sendSessionEvent(new SessionEvent(userId, minutes, updateStreak));
    }

    private void sendSessionEvent(SessionEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("session-events", message);
        } catch (Exception e) {
            log.error("Failed to send session event", e);
        }
    }
}
