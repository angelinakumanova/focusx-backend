package app.focusx.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public SessionEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNewSessionAddedEvent(String userId) {
        kafkaTemplate.send("session-events", userId);
    }
}
