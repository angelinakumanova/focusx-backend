package app.focusx.messaging.producer;

import app.focusx.messaging.event.SessionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SessionEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendNewSessionAddedEvent(String userId) {
        sendSessionEvent(new SessionEvent(userId, null));
    }

    public void sendSessionWithMinutes(String userId, long minutes) {
        sendSessionEvent(new SessionEvent(userId, minutes));
    }

    private void sendSessionEvent(SessionEvent event) {
        throw new RuntimeException("Failed to send session event");
//        try {
//            String message = objectMapper.writeValueAsString(event);
//            kafkaTemplate.send("session-events", message);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to send session event", e);
//        }
    }
}
