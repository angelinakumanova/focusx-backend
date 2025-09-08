package app.focusx.messaging.producer;

import app.focusx.messaging.event.NewSessionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NewSessionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NewSessionEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendNewSessionAddedEvent(String userId, long minutes, boolean updateStreak) {
        sendSessionEvent(new NewSessionEvent(userId, minutes, updateStreak));
    }

    private void sendSessionEvent(NewSessionEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("new-session-event", message);
        } catch (Exception e) {
            log.error("Failed to send new session event", e);
        }
    }
}
