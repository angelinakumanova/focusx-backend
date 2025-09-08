package app.focusx.messaging.producer;

import app.focusx.messaging.producer.event.VerifiedUserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VerifiedUserEventProducer {

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper mapper;

    public VerifiedUserEventProducer(KafkaTemplate<String, String> template, ObjectMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    public void sendVerifiedUserEvent(VerifiedUserEvent event) {
        sendEvent(event);
    }

    private void sendEvent(VerifiedUserEvent event) {
        try {
            String data = mapper.writeValueAsString(event);
            template.send("verified-user-event", data);
        } catch (Exception e) {
            log.error("Failed to send verified user event", e);
        }
    }
}
